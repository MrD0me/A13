package com.groom.manvsclass.model.filesystem;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.groom.manvsclass.model.ServiceURL;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

public class RobotUtil {
	public static final String VOLUME_T8_BASE_PATH = "/VolumeT8/FolderTree/ClassUT/";
	public static final String VOLUME_T9_BASE_PATH = "/VolumeT9/FolderTree/ClassUT/";
	public static final String BASE_SRC_PATH = "src/main/java";
	public static final String BASE_TEST_PATH = "src/test/java";
	public static final String BASE_COVERAGE_PATH = "coverage";

	//---------------------------------FUNZIONE UNZIP
	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());
	
		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();
	
		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}
	
		return destFile;
	}

	public static void unzip(String fileZip, File destDir) throws IOException {

		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = newFile(destDir, zipEntry);
			if (zipEntry.isDirectory()) {
				if (!newFile.isDirectory() && !newFile.mkdirs()) {
					throw new IOException("Failed to create directory " + newFile);
				}
			} else {
				// Aggiustamento per archivi creati con Windows
				File parent = newFile.getParentFile();
				if (!parent.isDirectory() && !parent.mkdirs()) {
					throw new IOException("Failed to create directory " + parent);
				}
			
				// Scrittura del contenuto del file
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}
	//--------------------------------------------------

	public static int[] getJacocoCoverageByCoverageType(String filePath, String coverageType) {
		try {
			Document doc = Jsoup.parse(new File(filePath), "UTF-8", "", Parser.xmlParser());
			// Selezione dell'elemento counter in base al tipo di copertura
			Element counter = doc.selectFirst("report > counter[type=" + coverageType + "]");

			if (counter == null) {
				throw new IllegalArgumentException("Elemento 'counter' di tipo '" + coverageType + "' non trovato nel documento XML.");
			}

			int covered = Integer.parseInt(counter.attr("covered"));
			int missed = Integer.parseInt(counter.attr("missed"));

			// Restituisce i due valori come array: [covered, missed]
			return new int[]{covered, missed};
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Gli attributi 'covered' e 'missed' devono essere numeri interi validi.", e);
		} catch (Exception e) {
			throw new RuntimeException("Errore durante l'elaborazione del documento XML.", e);
		}
	}

	/**
	 * @param path Il percorso del file XML contenente le informazioni di copertura.
	 * @return La percentuale di copertura delle linee.
	 */
	public static int[] getEmmaCoverageByCoverageType(String path, String coverageType) {
		try {
			File cov = new File(path);
			Document doc = Jsoup.parse(cov, null, "", Parser.xmlParser());

			// Seleziona solo il primo elemento che corrisponde al tipo di coverage richiesto
			Element stat = doc.selectFirst("coverage[type=\"" + coverageType + "\"]");

			if (stat == null) {
				throw new IllegalArgumentException("Nessuna riga trovata per il tipo di coverage: " + coverageType);
			}

			String value = stat.attr("value");
			Pattern pattern = Pattern.compile("\\((\\d+)/(\\d+)\\)");
			Matcher matcher = pattern.matcher(value);

			if (!matcher.find()) {
				throw new IllegalArgumentException("Formato valore non valido: " + value);
			}

			int covered = Integer.parseInt(matcher.group(1));
			int total = Integer.parseInt(matcher.group(2)) - Integer.parseInt(matcher.group(1));

			return new int[]{covered, total};
		} catch (IOException e) {
			throw new RuntimeException("Errore nella lettura del file XML.", e);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Gli attributi 'covered' e 'total' devono essere numeri interi validi.", e);
		} catch (Exception e) {
			throw new RuntimeException("Errore durante l'elaborazione del documento XML.", e);
		}
	}

	public static int[] getEvoSuiteCoverageStatistics(String filePath) {
		List<Integer> values = new ArrayList<>();
		String line;
		String delimiter = ",";

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			boolean firstLine = true; // salto la prima riga, che contiene i nomi delle colonne

			while ((line = br.readLine()) != null) {
				if (firstLine) {
					firstLine = false;
					continue;
				}

				String[] columns = line.split(delimiter);

				// Verifico che esistano almeno 3 colonne, la percentuale di coverage si trova sulla terza
				if (columns.length >= 3) {
					try {
						double value = Double.parseDouble(columns[2].trim()) * 100;
						values.add((int) value);
					} catch (NumberFormatException e) {
						System.err.println("Errore nella conversione a intero: " + e);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Converto la lista in array di interi
		return values.stream().mapToInt(i -> i).toArray();
	}

	public static void caricaFile(String fileName, Path directory, MultipartFile file) throws IOException{
		try {
			// Verifica se la directory esiste già
			if (!Files.exists(directory)) {
				// Crea la directory
				Files.createDirectories(directory);
				System.out.println("La directory è stata creata con successo.");
			} else {
				System.out.println("La directory esiste già.");
			}
		} catch (Exception e) {
			System.out.println("Errore durante la creazione della directory: " + e.getMessage());
		}
		// Legge l'input stream del file caricato e lo copia nella directory specificata
		try (InputStream inputStream = file.getInputStream()) {
			// Risolve il percorso completo del file all'interno della directory
			// specificata.
			// Viene utilizzato il metodo 'directory.resolve(fileNameClass)' per ottenere il
			// percorso completo
			// del file all'interno della directory 'directory'. Questo percorso completo
			// sarà utilizzato
			// successivamente per copiare il contenuto dell'input stream del file nella
			// posizione desiderata.
			Path filePath = directory.resolve(fileName);
			System.out.println(filePath.toString());

			// copio il contenuto dell'input stream nel file di destinaziione
			// l'ultimo parametro di questa funzione indica che se il file già esiste deve
			// essere sostituito
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

			// chiusura dell'input stream dopo aver completato la copia
			inputStream.close();
		}
	}

	public static void outputProcess(Process process) throws IOException{
		// Legge l'output del processo esterno tramite un BufferedReader, che a sua
		// volta usa
		// un InputStreamReader per convertire i byte in caratteri. Il metodo
		// 'process.getInputStream()'
		// restituisce lo stream di input del processo esterno.
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

		// All'interno del loop viene letta ogni linea disponibile finché il processo
		// continua a produrre output.
        while ((line = reader.readLine()) != null)
            System.out.println(line);
		
		// funzionamento analogo al precedente, invece di leggere l'output leggiamo gli
		// errori
        reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = reader.readLine()) != null)
            System.out.println(line);

        try {
			// Attende che il processo termini e restituisce il codice di uscita
			int exitCode = process.waitFor();

			System.out.println("ERRORE CODE: " + exitCode);
		} catch (InterruptedException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public static void uploadRobotCoverageInT4(int[] evoSuiteStatistics, int[][] jacocoStatistics, int livello, String className, String robotName) throws IOException{
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPost = new HttpPost("http://" + ServiceURL.T4.getServiceURL() + "/robots");

		// Creazione di un array JSON per contenere le informazioni sui robot generati
		JSONArray arr = new JSONArray();

		// Creazione di un oggetto JSON per rappresentare un singolo robot generato
		JSONObject rob = new JSONObject();

		// l'array JSON viene utilizzato per raggruppare gli oggetti JSON che
		// rappresentano le informazioni sui robot generati.
		// L'array arr contiene una serie di oggetti rob, ognuno dei quali rappresenta


		// Aggiunge al robot l'informazione relativa al punteggio convertito in stringa
		rob.put("jacocoLineCovered", jacocoStatistics[0][0]);
		rob.put("jacocoLineMissed", jacocoStatistics[0][1]);
		rob.put("jacocoBranchCovered", jacocoStatistics[1][0]);
		rob.put("jacocoBranchMissed", jacocoStatistics[1][1]);
		rob.put("jacocoInstructionCovered", jacocoStatistics[2][0]);
		rob.put("jacocoInstructionMissed", jacocoStatistics[2][1]);

		rob.put("evoSuiteBranch", evoSuiteStatistics[0]);
		rob.put("evoSuiteException", evoSuiteStatistics[1]);
		rob.put("evoSuiteWeakMutation", evoSuiteStatistics[2]);
		rob.put("evoSuiteOutput", evoSuiteStatistics[3]);
		rob.put("evoSuiteMethod", evoSuiteStatistics[4]);
		rob.put("evoSuiteMethodNoException", evoSuiteStatistics[5]);
		rob.put("evoSuiteCBranch", evoSuiteStatistics[6]);
		// aggiunge al robot l'informazione relativa a quale robot è stato utilizzato,
		rob.put("type", robotName);

		// aggiunge al robot l'informazione riguardante il livello di difficoltà
		// converitto in stringa
		rob.put("difficulty", String.valueOf(livello));

		// aggiunge al roboto l'informazione relativa all'id della classe di test
		rob.put("testClassId", className);

		// Aggiunge l'oggetto robot all'array JSON
		arr.put(rob);

		// Crea un oggetto JSON principale contenente l'array di robot
		JSONObject obj = new JSONObject();

		// inserimento dell'array di robot all'interno dell'oggetto
		obj.put("robots", arr);

		// Crea un'entità JSON utilizzando il contenuto dell'oggetto JSON principale.
		StringEntity jsonEntity = new StringEntity(obj.toString(), ContentType.APPLICATION_JSON);

		System.out.println(EntityUtils.toString(jsonEntity));

		// Configura la richiesta POST con l'entità JSON creata
		httpPost.setEntity(jsonEntity);

		// esegue la richiesta ed ottiene la risposta
		HttpResponse response = httpClient.execute(httpPost);
	}

	/**
	 * Genera e salva i file di robot e ne calcola la copertura delle linee.
	 *
	 * @param fileName      Il nome del file della classe.
	 * @param className     Il nome della classe di test.
	 * @param classFile     Il file caricato.
	 * @throws IOException Eccezione di IO.
	 */
    public static void generateAndSaveRobots(String fileName, String className, MultipartFile classFile) throws IOException {

        // RANDOOP - T9			    
		Path directory = Paths.get("/VolumeT9/FolderTree/ClassUT/" + className + "/src/main/java");
		caricaFile(fileName, directory, classFile);
		
		//Randoop T9
		// creazione del processo esterno di generazione dei test
        ProcessBuilder processBuilder = new ProcessBuilder();

		// con command si configura il comando del processo esterno per eseguire il file
		// JAR 'Task9-G19-0.0.1-SNAPSHOT.jar'
		// l'esecuzione avviene attraverso la JVM di Java.
		// Il parametro "-jar" specifica l'esecuzione di un file JAR.
        processBuilder.command("java", "-jar", "Task9-G19-0.0.1-SNAPSHOT.jar");

		// La directory di lavoro per il processo esterno viene impostata su
		// "/VolumeT9/" utilizzando
		// questo metodo garantisce che il processo lavori nella directory desiderata
        processBuilder.directory(new File("/VolumeT9/"));
		
		// linea di debugg--potremmo anche commentarla
		System.out.println("Prova");
		
		// si avvia il processo
        Process process = processBuilder.start();

		//Legge l'output del processo appena creato
		outputProcess(process);

		File robotCoverageDirBasePath = new File(String.format("%s/%s/%s", VOLUME_T9_BASE_PATH, className, BASE_COVERAGE_PATH));
		String robotName = "randoop";
		for (File levelFolder : Objects.requireNonNull(robotCoverageDirBasePath.listFiles())) {
			String emmaCoveragePath = String.format("%s/%s", levelFolder, "coveragetot.xml");

			int[] evoSuiteStatistics = getEvoSuiteCoverageStatistics(String.format("%s/%s", levelFolder, "statistics.csv"));
			int[][] emmaStatistics = {
					getEmmaCoverageByCoverageType(emmaCoveragePath, "line, %"),
					getEmmaCoverageByCoverageType(emmaCoveragePath, "method, %"),
					getEmmaCoverageByCoverageType(emmaCoveragePath, "block, %")
			};

			int level = Integer.parseInt(levelFolder.toString().substring(levelFolder.toString().length() - 7, levelFolder.toString().length() - 5));

			uploadRobotCoverageInT4(evoSuiteStatistics, emmaStatistics, level, className, robotName);

		}


		// Il seguente codice è l'adattamento ad evosuite del codice appena visto, i
		// passaggi sono gli stessi
        // EVOSUITE - T8
		// TODO: RICHIEDE AGGIUSTAMENTI IN T8
		Path directoryE = Paths.get("/VolumeT8/FolderTree/ClassUT/" + className + "/src/main/java");

		caricaFile(fileName, directoryE, classFile);

		ProcessBuilder processBuilderE = new ProcessBuilder();

        processBuilderE.command("bash", "robot_generazione.sh", className, "\"\"", "/VolumeT9/FolderTree/ClassUT/" + className + "/src/main/java", String.valueOf("1"));
        processBuilderE.directory(new File("/VolumeT8/Prototipo2.0/"));

		Process processE = processBuilderE.start();

		outputProcess(processE);

		robotCoverageDirBasePath = new File(String.format("%s/%s/%s", VOLUME_T8_BASE_PATH, className, BASE_COVERAGE_PATH));
		robotName = "evosuite";
		for (File levelFolder : Objects.requireNonNull(robotCoverageDirBasePath.listFiles())) {
			String jacocoCoveragePath = String.format("%s/%s", levelFolder, "coveragetot.xml");

			int[] evoSuiteStatistics = getEvoSuiteCoverageStatistics(String.format("%s/%s", levelFolder, "statistics.csv"));
			int[][] jacocoStatistics = {
					getJacocoCoverageByCoverageType(jacocoCoveragePath, "LINE"),
					getJacocoCoverageByCoverageType(jacocoCoveragePath, "BRANCH"),
					getJacocoCoverageByCoverageType(jacocoCoveragePath, "INSTRUCTION")
			};

			int level = Integer.parseInt(levelFolder.toString().substring(levelFolder.toString().length() - 7, levelFolder.toString().length() - 5));

			uploadRobotCoverageInT4(evoSuiteStatistics, jacocoStatistics, level, className, robotName);

		}

    }

	private static void writeStringToFile(String content, File file) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(content);
		}
	}

	public static void deleteDirectoryRecursively(Path dirPath) throws IOException {
		if (Files.isDirectory(dirPath)) {
			Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc != null) {
						throw exc; // Solleva l'eccezione se la directory non può essere cancellata
					}
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	public static void copyDirectoryRecursively(Path sourcePath, Path destinationPath) throws IOException {
		if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
			throw new IllegalArgumentException("Il percorso sorgente non esiste o non è una directory.");
		}

		Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path targetDir = destinationPath.resolve(sourcePath.relativize(dir));
				Files.createDirectories(targetDir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path targetFile = destinationPath.resolve(sourcePath.relativize(file));
				Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void uploadEvoSuiteInVolume(Path tmpPath, Path testPath, Path coveragePath) throws IOException {
		Path zipFile;
		try {
			zipFile = Objects.requireNonNull((new File(String.valueOf(tmpPath))).listFiles())[0].toPath();
		} catch (NullPointerException e) {
			return;
		}

		RobotUtil.unzip(String.valueOf(zipFile), tmpPath.toFile());
		Files.delete(zipFile);

		for (File levelFolder : Objects.requireNonNull(tmpPath.toFile().listFiles())) {
			String level = levelFolder.getName();
			for (File src : Objects.requireNonNull(new File(String.format("%s/%s", levelFolder.getPath(), "TestSourceCode/evosuite-tests")).listFiles())) {
				Files.createDirectories(Paths.get(String.format("%s/%s", testPath, level)));
				Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s", testPath, level, src.getName())));
			}

			for (File src : Objects.requireNonNull(new File(String.format("%s/%s", levelFolder.getPath(), "TestReport")).listFiles())) {
				Files.createDirectories(Paths.get(String.format("%s/%s", coveragePath, level)));
				Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s", coveragePath, level, src.getName())));
			}
		}
	}

	private static void uploadRandoopInVolume(Path tmpPath, Path testPath, Path coveragePath, boolean ignoreCoverageFile) throws IOException {
		Path zipFile;
		try {
			zipFile = Objects.requireNonNull((new File(String.valueOf(tmpPath))).listFiles())[0].toPath();
		} catch (NullPointerException e) {
			return;
		}

		RobotUtil.unzip(String.valueOf(zipFile), tmpPath.toFile());
		Files.delete(zipFile);

		for (File levelFolder : Objects.requireNonNull(tmpPath.toFile().listFiles())) {
			String level = levelFolder.getName();
			for (File src : Objects.requireNonNull(new File(levelFolder.getPath()).listFiles())) {
				if (src.getName().endsWith(".java")) {
					Files.createDirectories(Paths.get(String.format("%s/%s", testPath, level)));
					Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s", testPath, level, src.getName())));
				} else if (!ignoreCoverageFile && src.getName().endsWith(".xml")) {
					Files.createDirectories(Paths.get(String.format("%s/%s", coveragePath, level)));
					Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s", coveragePath, level, src.getName())));
				}
			}
		}
	}

	public static void generateMissingEvoSuiteCoverage(String className, Path tmpSrcPath, Path tmpTestPath, Path tmpCoveragePath, Path tmpPath, Path coveragePath) {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost httpPostT8 = new HttpPost("http://" + ServiceURL.T8.getServiceURL() + "/coverage/randoop");

			// Creazione del body JSON
			JSONObject reqBody = new JSONObject();
			reqBody.put("evoSuitWorkingDir", "/VolumeT8/FolderTree/ClassUT/" + className);
			reqBody.put("sourceClassPath", tmpSrcPath);
			reqBody.put("sourceClassName", className);
			reqBody.put("testClassPath", tmpTestPath);
			reqBody.put("saveDirPath", tmpCoveragePath);

			// Imposta il body della richiesta
			StringEntity entity = new StringEntity(reqBody.toString(), ContentType.APPLICATION_JSON);
			System.out.println(reqBody);
			httpPostT8.setEntity(entity);

			// Esegue la richiesta HTTP
			try (CloseableHttpResponse response = httpClient.execute(httpPostT8)) {
				JSONObject responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
				System.out.println("Response Body: " + responseBody);

				for (File levelFolder : Objects.requireNonNull(tmpPath.toFile().listFiles())) {
					String level = levelFolder.getName();
					writeStringToFile(responseBody.get(level).toString(), new File(String.format("%s/%s/%s", coveragePath, level, "statistics.csv")));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void generateMissingJacocoCoverage(Path tmpPath, Path srcPath, Path testPath, Path coveragePath) throws IOException {
		for (File levelFolder : Objects.requireNonNull(testPath.toFile().listFiles())) {
			Files.createDirectories(Paths.get(String.format("%s/%s", tmpPath, Paths.get(BASE_SRC_PATH))));
			copyDirectoryRecursively(srcPath, Paths.get(String.format("%s/%s", tmpPath, Paths.get(BASE_SRC_PATH))));

			Files.createDirectories(Paths.get(String.format("%s/%s", tmpPath, Paths.get(BASE_TEST_PATH))));
			copyDirectoryRecursively(levelFolder.toPath(), Paths.get(String.format("%s/%s", tmpPath, Paths.get(BASE_TEST_PATH))));

			File zip = null;
			String level = levelFolder.getName();
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				ZipUtils.zipDirectory(String.format("%s/src", tmpPath), "/VolumeT8/FolderTree/ClassUT/src.zip");
				zip = new File("/VolumeT8/FolderTree/ClassUT/src.zip");

				if (!zip.exists()) {
					System.err.println("Errore: Il file ZIP non è stato creato correttamente.");
					return;
				}

				HttpPost httpPost = new HttpPost("http://" + ServiceURL.T7.getServiceURL() + "/coverage/evosuite");

				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.STRICT); // Assicura compatibilità
				builder.addBinaryBody("evoSuiteCode", zip);

				HttpEntity multipart = builder.build();
				httpPost.setEntity(multipart);
				httpPost.setHeader("Accept", "application/json");

				try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
					JSONObject responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
					System.out.println("Response Body: " + responseBody);

					writeStringToFile(responseBody.getString("coverage"), new File(String.format("%s/%s/%s", coveragePath, level, "coveragetot.xml")));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			Files.delete(zip.toPath());
		}
	}

	public static void saveRobots(String fileNameClass, String fileNameTestRandoop, String fileNameTestEvoSuit , String className, MultipartFile classFile, MultipartFile testFileRandoop, MultipartFile testFileEvoSuit)
			throws IOException {

		/*CARICAMENTO CLASSE NEI VOLUMI T8 E T9*/
		Path srcCodeT8 = Paths.get(String.format("%s/%s/%s", VOLUME_T8_BASE_PATH, className, BASE_SRC_PATH));
		Path testCodeT8 = Paths.get(String.format("%s/%s/%s", VOLUME_T8_BASE_PATH, className, BASE_TEST_PATH));
		Path coverageT8 = Paths.get(String.format("%s/%s/%s", VOLUME_T8_BASE_PATH, className, BASE_COVERAGE_PATH));

		Path srcCodeT9 = Paths.get(String.format("%s/%s/%s", VOLUME_T9_BASE_PATH, className, BASE_SRC_PATH));
		Path testCodeT9 = Paths.get(String.format("%s/%s/%s", VOLUME_T9_BASE_PATH, className, BASE_TEST_PATH));
		Path coverageT9 = Paths.get(String.format("%s/%s/%s", VOLUME_T9_BASE_PATH, className, BASE_COVERAGE_PATH));

		Path tmpFolder = Paths.get(String.format("%s/%s/tmp", VOLUME_T8_BASE_PATH, className));

		caricaFile(fileNameClass, srcCodeT8, classFile);
		caricaFile(fileNameClass, srcCodeT9, classFile);
		caricaFile(fileNameTestEvoSuit, tmpFolder, testFileEvoSuit);

		uploadEvoSuiteInVolume(tmpFolder, testCodeT8, coverageT8);
		deleteDirectoryRecursively(tmpFolder);
		/*
		Path zipFile;
		try {
			zipFile = Objects.requireNonNull((new File(String.valueOf(tmpFolder))).listFiles())[0].toPath();
		} catch (NullPointerException e) {
			return;
		}

		RobotUtil.unzip(String.valueOf(zipFile), tmpFolder.toFile());
		Files.delete(zipFile);

		for (File levelFolder : Objects.requireNonNull(tmpFolder.toFile().listFiles())) {
			String level = levelFolder.getName();
			for (File src : Objects.requireNonNull(new File(String.format("%s/%s", levelFolder.getPath(), "TestSourceCode/evosuite-tests")).listFiles())) {
				Files.createDirectories(Paths.get(String.format("%s/%s", testCodeT8, level)));
				Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s", testCodeT8, level, src.getName())));
			}

			for (File src : Objects.requireNonNull(new File(String.format("%s/%s", levelFolder.getPath(), "TestReport")).listFiles())) {
				Files.createDirectories(Paths.get(String.format("%s/%s/%s/%s", basePathT8, className, coveragePath, level)));
				Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s/%s/%s", basePathT8, className, coveragePath, level, src.getName())));
			}
		}

		deleteDirectoryRecursively(tmpFolder);

		 */


		tmpFolder = Paths.get(String.format("%s/%s/tmp", VOLUME_T9_BASE_PATH, className));
		caricaFile(fileNameTestRandoop, tmpFolder, testFileRandoop);

		uploadRandoopInVolume(tmpFolder, testCodeT9, coverageT9, false);
		deleteDirectoryRecursively(tmpFolder);

		/*
		try {
			zipFile = Objects.requireNonNull((new File(String.valueOf(tmpFolder))).listFiles())[0].toPath();
		} catch (NullPointerException e) {
			return;
		}

		RobotUtil.unzip(String.valueOf(zipFile), tmpFolder.toFile());
		Files.delete(zipFile);

		for (File levelFolder : Objects.requireNonNull(tmpFolder.toFile().listFiles())) {
			String level = levelFolder.getName();
			for (File src : Objects.requireNonNull(new File(levelFolder.getPath()).listFiles())) {
				if (src.getName().endsWith(".java")) {
					Files.createDirectories(Paths.get(String.format("%s/%s", testCodeT9, level)));
					Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s", testCodeT9, level, src.getName())));
				} else if (src.getName().endsWith(".xml")) {
					Files.createDirectories(Paths.get(String.format("%s/%s/%s/%s", basePathT9, className, coveragePath, level)));
					Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s/%s/%s", basePathT9, className, coveragePath, level, src.getName())));
				}
			}
		}

		deleteDirectoryRecursively(tmpFolder);

		 */

		tmpFolder = Paths.get(String.format("%s/%s/tmp", VOLUME_T8_BASE_PATH, className));
		Path srcCodeT8_RandoopTmp = Paths.get(String.format("%s/%s/%s", VOLUME_T8_BASE_PATH, className + "_RandoopTmp", BASE_SRC_PATH));
		Path testCodeT8_RandoopTmp = Paths.get(String.format("%s/%s/%s", VOLUME_T8_BASE_PATH, className + "_RandoopTmp", BASE_TEST_PATH));
		Path coverageT8_RandoopTmp = Paths.get(String.format("%s/%s/%s", VOLUME_T8_BASE_PATH, className + "_RandoopTmp", BASE_COVERAGE_PATH));
		caricaFile(fileNameClass, srcCodeT8_RandoopTmp, classFile);
		caricaFile(fileNameTestRandoop, tmpFolder, testFileRandoop);

		uploadRandoopInVolume(tmpFolder, testCodeT8_RandoopTmp, coverageT8_RandoopTmp, true);
		/*
		try {
			zipFile = Objects.requireNonNull((new File(String.valueOf(tmpFolder))).listFiles())[0].toPath();
		} catch (NullPointerException e) {
			return;
		}

		RobotUtil.unzip(String.valueOf(zipFile), tmpFolder.toFile());
		Files.delete(zipFile);

		for (File levelFolder : Objects.requireNonNull(tmpFolder.toFile().listFiles())) {
			System.out.println(tmpFolder);
			System.out.println(levelFolder.getName());
			String level = levelFolder.getName();
			for (File src : Objects.requireNonNull(new File(levelFolder.getPath()).listFiles())) {
				if (src.getName().endsWith(".java")) {
					Files.createDirectories(Paths.get(String.format("%s/%s", testCodeT8_RandoopTmp, level)));
					Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s", testCodeT8_RandoopTmp, level, src.getName())));
				} else if (src.getName().endsWith(".xml")) {
					Files.createDirectories(Paths.get(String.format("%s/%s", coverageT8_RandoopTmp, level)));
					Files.copy(src.toPath(), Paths.get(String.format("%s/%s/%s", coverageT8_RandoopTmp, level, src.getName())));
				}
			}
		}

		 */

		generateMissingEvoSuiteCoverage(className, srcCodeT8_RandoopTmp, testCodeT8_RandoopTmp, coverageT8_RandoopTmp, tmpFolder, coverageT9);

		/*
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost httpPostT8 = new HttpPost("http://" + ServiceURL.T8.getServiceURL() + "/coverage/randoop");

			// Creazione del body JSON
			JSONObject reqBody = new JSONObject();
			reqBody.put("evoSuitWorkingDir", "/VolumeT8/FolderTree/ClassUT/" + className);
			reqBody.put("sourceClassPath", srcCodeT8_RandoopTmp);
			reqBody.put("sourceClassName", className);
			reqBody.put("testClassPath", testCodeT8_RandoopTmp);
			reqBody.put("saveDirPath", Paths.get(String.format("%s/%s/%s", testCodeT8_RandoopTmp, className, coveragePath)));

			// Imposta il body della richiesta
			StringEntity entity = new StringEntity(reqBody.toString(), ContentType.APPLICATION_JSON);
			System.out.println(reqBody);
			httpPostT8.setEntity(entity);

			// Esegue la richiesta HTTP
			try (CloseableHttpResponse response = httpClient.execute(httpPostT8)) {
				JSONObject responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
				System.out.println("Response Body: " + responseBody);

				for (File levelFolder : Objects.requireNonNull(tmpFolder.toFile().listFiles())) {
					String level = levelFolder.getName();
					writeStringToFile(responseBody.get(level).toString(), new File(String.format("%s/%s/%s/%s/%s", basePathT9, className, coveragePath, level, "statistics.csv")));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		 */

		deleteDirectoryRecursively(tmpFolder);
		deleteDirectoryRecursively(Paths.get(String.format("%s/%s", VOLUME_T8_BASE_PATH, className + "_RandoopTmp")));

		generateMissingJacocoCoverage(tmpFolder, srcCodeT8, testCodeT8, coverageT8);
		/*
		for (File levelFolder : Objects.requireNonNull(testCodeT8.toFile().listFiles())) {
			Files.createDirectories(Paths.get(String.format("%s/%s", tmpFolder, srcPath)));
			copyDirectoryRecursively(srcCodeT8, Paths.get(String.format("%s/%s", tmpFolder, srcPath)));

			Files.createDirectories(Paths.get(String.format("%s/%s", tmpFolder, testPath)));
			copyDirectoryRecursively(levelFolder.toPath(), Paths.get(String.format("%s/%s", tmpFolder, testPath)));

			File zip = null;
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				ZipUtils.zipDirectory(String.format("%s/src", tmpFolder), "/VolumeT8/FolderTree/ClassUT/src.zip");
				zip = new File("/VolumeT8/FolderTree/ClassUT/src.zip");

				if (!zip.exists()) {
					System.err.println("Errore: Il file ZIP non è stato creato correttamente.");
					return;
				}

				HttpPost httpPost = new HttpPost("http://" + ServiceURL.T7.getServiceURL() + "/coverage/evosuit");

				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.setMode(HttpMultipartMode.STRICT); // Assicura compatibilità
				builder.addBinaryBody("evoSuiteCode", zip);

				HttpEntity multipart = builder.build();
				httpPost.setEntity(multipart);
				httpPost.setHeader("Accept", "application/json");

				try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
					JSONObject responseBody = new JSONObject(EntityUtils.toString(response.getEntity()));
					System.out.println("Response Body: " + responseBody);

					writeStringToFile(responseBody.getString("coverage"), new File(String.format("%s/%s/%s/%s/%s", basePathT8, className, coveragePath, "01Level", "coveragetot.xml")));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			Files.delete(zip.toPath());
		}

		 */
		deleteDirectoryRecursively(tmpFolder);

		/* SALVATAGGIO RISULTATI NEL TASK T4 */

		File robotCoverageDirBasePath = new File(String.format("%s/%s/%s", VOLUME_T8_BASE_PATH, className, BASE_COVERAGE_PATH));
		String robotName = "evosuite";
		for (File levelFolder : Objects.requireNonNull(robotCoverageDirBasePath.listFiles())) {
			String jacocoCoveragePath = String.format("%s/%s", levelFolder, "coveragetot.xml");

			int[] evoSuiteStatistics = getEvoSuiteCoverageStatistics(String.format("%s/%s", levelFolder, "statistics.csv"));
			int[][] jacocoStatistics = {
				getJacocoCoverageByCoverageType(jacocoCoveragePath, "LINE"),
				getJacocoCoverageByCoverageType(jacocoCoveragePath, "BRANCH"),
				getJacocoCoverageByCoverageType(jacocoCoveragePath, "INSTRUCTION")
			};

			int level = Integer.parseInt(levelFolder.toString().substring(levelFolder.toString().length() - 7, levelFolder.toString().length() - 5));

			uploadRobotCoverageInT4(evoSuiteStatistics, jacocoStatistics, level, className, robotName);

		}

		robotCoverageDirBasePath = new File(String.format("%s/%s/%s", VOLUME_T9_BASE_PATH, className, BASE_COVERAGE_PATH));
		robotName = "randoop";
		for (File levelFolder : Objects.requireNonNull(robotCoverageDirBasePath.listFiles())) {
			String emmaCoveragePath = String.format("%s/%s", levelFolder, "coveragetot.xml");

			int[] evoSuiteStatistics = getEvoSuiteCoverageStatistics(String.format("%s/%s", levelFolder, "statistics.csv"));
			int[][] emmaStatistics = {
					getEmmaCoverageByCoverageType(emmaCoveragePath, "line, %"),
					getEmmaCoverageByCoverageType(emmaCoveragePath, "method, %"),
					getEmmaCoverageByCoverageType(emmaCoveragePath, "block, %")
			};

			int level = Integer.parseInt(levelFolder.toString().substring(levelFolder.toString().length() - 7, levelFolder.toString().length() - 5));

			uploadRobotCoverageInT4(evoSuiteStatistics, emmaStatistics, level, className, robotName);

		}
	}
    

}