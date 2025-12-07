package com.groom.manvsclass.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groom.manvsclass.api.ApiGatewayClient;
import com.groom.manvsclass.model.ClassUT;
import com.groom.manvsclass.model.Operation;
import com.groom.manvsclass.model.Opponent;
import com.groom.manvsclass.model.repository.ClassRepository;
import com.groom.manvsclass.model.repository.OperationRepository;
import com.groom.manvsclass.model.repository.OpponentRepository;
import com.groom.manvsclass.model.repository.SearchRepositoryImpl;
import com.groom.manvsclass.service.exception.CoverageNotFoundException;
import com.groom.manvsclass.service.exception.OpponentNotFoundException;
import com.groom.manvsclass.service.exception.ScoreNotFoundException;
import com.groom.manvsclass.util.filesystem.FileOperationUtil;
import com.groom.manvsclass.util.filesystem.download.FileDownloadUtil;
import com.groom.manvsclass.util.filesystem.upload.FileUploadResponse;
import com.groom.manvsclass.util.filesystem.upload.FileUploadUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import testrobotchallenge.commons.models.opponent.OpponentDifficulty;
import testrobotchallenge.commons.models.score.EvosuiteScore;
import testrobotchallenge.commons.models.score.JacocoScore;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OpponentService {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(String.valueOf(OpponentService.class));
    private final OperationRepository operationRepository;
    private final ClassRepository classRepository;
    private final SearchRepositoryImpl searchRepository;
    private final UploadOpponentService uploadOpponentService;
    private final OpponentRepository opponentRepository;
    private final ApiGatewayClient apiGatewayClient;

    public OpponentService(OperationRepository operationRepository,
                           ClassRepository classRepository,
                           SearchRepositoryImpl searchRepository,
                           UploadOpponentService uploadOpponentService,
                           OpponentRepository opponentRepository,
                           ApiGatewayClient apiGatewayClient) {
        this.operationRepository = operationRepository;
        this.classRepository = classRepository;
        this.searchRepository = searchRepository;
        this.uploadOpponentService = uploadOpponentService;
        this.opponentRepository = opponentRepository;
        this.apiGatewayClient = apiGatewayClient;
    }

    /*
     * Restituisce la lista di classi UT disponibili nel sistema
     */
    public ResponseEntity<?> getNomiClassiUT(String jwt) {
        List<String> classNames = classRepository.findAll()
                .stream()
                .map(ClassUT::getName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(classNames);
    }

    public ResponseEntity<FileUploadResponse> uploadOpponent(
            MultipartFile classUTFile,
            String classUTDetails,
            MultipartFile robotTestsZip) throws IOException {

        FileUploadResponse response = new FileUploadResponse();

        if (classUTFile == null || classUTFile.isEmpty()) {
            response.setErrorMessage("Errore: file della classe non ricevuto o vuoto.");
            return ResponseEntity.badRequest().body(response);
        }

        ObjectMapper mapper = new ObjectMapper();
        ClassUT classe = mapper.readValue(classUTDetails, ClassUT.class);

        String classUTFileName = StringUtils.cleanPath(Objects.requireNonNull(classUTFile.getOriginalFilename()));
        long size = classUTFile.getSize();

        System.out.println("Salvataggio di " + classUTFileName + " nel filesystem condiviso");

        FileUploadUtil.saveCLassFile(classUTFileName, classe.getName(), classUTFile);
        uploadOpponentService.saveOpponentsFromZip(classUTFileName, classe.getName(), classUTFile, robotTestsZip);

        response.setFileName(classUTFileName);
        response.setSize(size);
        response.setDownloadUri("/downloadFile");

        classe.setUri(String.format("%s/%s/%s/%s",
                UploadOpponentService.VOLUME_T0_BASE_PATH,
                UploadOpponentService.UNMODIFIED_SRC,
                classe.getName(),
                classUTFileName));

        classe.setDate(LocalDate.now().toString());

        classRepository.save(classe);

        System.out.println("Operazione completata con successo (uploadTest)");

        return ResponseEntity.ok(response);
    }


    public ResponseEntity<?> downloadClasse(@PathVariable("name") String name) throws Exception {

        System.out.println("/downloadFile/{name} (HomeController) - name: " + name);
        System.out.println("test");
        try {
            List<ClassUT> classe = searchRepository.findByText(name);
            if (classe.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ClasseUT " + name + " non trovata");
            }
            System.out.println("File download:");
            System.out.println(classe.get(0).getUri());
            ResponseEntity file = FileDownloadUtil.downloadClassFile(classe.get(0).getUri());
            return file;
        } catch (Exception e) {
            System.out.println("Classe UT non trovata");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ClasseUT " + name + " non trovata");
        }
    }

    public List<ClassUT> getAllClassUTs() {
        return classRepository.findAll();
    }

    public List<ClassUT> filterByDifficulty(String difficulty) {
        return searchRepository.filterByDifficulty(difficulty);
    }

    public List<ClassUT> orderByDate() {
        return searchRepository.orderByDate();
    }

    public List<ClassUT> orderByName() {
        return searchRepository.orderByName();
    }

    public ResponseEntity<String> modificaClasse(String name, ClassUT newContent, String jwt, HttpServletRequest request) {
        return classRepository.findById(name)
                .map(existing -> {
                    String updatedName = newContent.getName() != null ? newContent.getName() : existing.getName();
                    boolean renamed = updatedName != null && !updatedName.equals(name);

                    existing.setName(updatedName);
                    existing.setDate(newContent.getDate() != null ? newContent.getDate() : existing.getDate());
                    existing.setDifficulty(newContent.getDifficulty() != null ? newContent.getDifficulty() : existing.getDifficulty());
                    existing.setDescription(newContent.getDescription() != null ? newContent.getDescription() : existing.getDescription());
                    existing.setCategory(newContent.getCategory() != null ? newContent.getCategory() : existing.getCategory());

                    if (renamed) {
                        classRepository.deleteById(name);
                    }
                    classRepository.save(existing);

                    LocalDate currentDate = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    String data = currentDate.format(formatter);
                    Operation operation1 = new Operation(null, "userAdmin", newContent.getName(), 1, data);
                    operationRepository.save(operation1);
                    return new ResponseEntity<>("Aggiornamento eseguito correttamente.", HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>("Nessuna classe trovata o nessuna modifica effettuata.", HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<?> eliminaClasse(String name) {
        Optional<ClassUT> classUTOptional = classRepository.findById(name);
        if (classUTOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Classe non trovata");
        }

        eliminaFile(name);
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String data = currentDate.format(formatter);
        Operation operation1 = new Operation(null, "userAdmin", name, 2, data);
        operationRepository.save(operation1);

        classRepository.deleteById(name);
        opponentRepository.deleteByClassUT(name);

        apiGatewayClient.callDeleteAllClassUTOpponents(name);
        return ResponseEntity.ok().body(classUTOptional.get());
    }

    public void eliminaFile(String fileName) {
        File directory = new File(String.format("%s/%s", UploadOpponentService.VOLUME_T0_BASE_PATH, fileName));
        File directoryUnmodifiedSrc = new File(String.format("%s/%s/%s", UploadOpponentService.VOLUME_T0_BASE_PATH, UploadOpponentService.UNMODIFIED_SRC, fileName));

        System.out.println("name: " + fileName);
        if (directory.exists() && directory.isDirectory()) {
            try {
                FileOperationUtil.deleteDirectoryRecursively(directory.toPath());
                FileOperationUtil.deleteDirectoryRecursively(directoryUnmodifiedSrc.toPath());
                logger.info("Cartella eliminata con successo (/deleteFile/{fileName})");
            } catch (IOException e) {
                throw new RuntimeException("Impossibile eliminare la cartella.");
            }
        } else {
            throw new RuntimeException("Cartella non trovata.");
        }
    }


    public List<Opponent> getAllOpponents() {
        return opponentRepository.findAll();
    }

    public Opponent getOpponentData(String classUT, String opponentType, OpponentDifficulty opponentDifficulty) {
        Optional<Opponent> opponent = opponentRepository.findByClassUTAndOpponentTypeAndOpponentDifficulty(classUT, opponentType, opponentDifficulty);
        if (opponent.isEmpty())
            throw new OpponentNotFoundException();

        return opponent.get();
    }

    public EvosuiteScore getOpponentEvosuiteScore(String classUT, String opponentType, OpponentDifficulty opponentDifficulty) {
        Optional<EvosuiteScore> score = opponentRepository.findEvosuiteScore(classUT,
                opponentType, opponentDifficulty);

        if (score.isEmpty())
            throw new ScoreNotFoundException();

        return score.get();
    }

    public JacocoScore getOpponentJacocoScore(String classUT, String opponentType, OpponentDifficulty opponentDifficulty) {
        Optional<JacocoScore> score = opponentRepository.findJacocoScore(classUT,
                opponentType, opponentDifficulty);

        if (score.isEmpty())
            throw new ScoreNotFoundException();

        return score.get();
    }

    public String getOpponentCoverage(String classUT, String opponentType, OpponentDifficulty opponentDifficulty) {
        Optional<String> coverage = opponentRepository.findCoverage(classUT,
                opponentType, opponentDifficulty);

        if (coverage.isEmpty())
            throw new CoverageNotFoundException();

        return coverage.get();
    }
}
