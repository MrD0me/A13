package robot

import (
	"fmt"
	"log"
	"math/rand"

	"github.com/alarmfox/game-repository/api"
	"github.com/alarmfox/game-repository/model"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

type RobotStorage struct {
	db *gorm.DB
}

func NewRobotStorage(db *gorm.DB) *RobotStorage {
	return &RobotStorage{
		db: db,
	}
}

func (rs *RobotStorage) CreateBulk(r *CreateRequest) (int, error) {
	robots := make([]model.Robot, len(r.Robots))

	log.Println(r)

	for i, robot := range r.Robots {
		robots[i] = model.Robot{
			TestClassId:               robot.TestClassId,
			Difficulty:                robot.Difficulty,
			Type:                      robot.Type.AsInt8(),
			JacocoLineCovered:         robot.JacocoLineCovered,
			JacocoBranchCovered:       robot.JacocoBranchCovered,
			JacocoInstructionCovered:  robot.JacocoInstructionCovered,
			JacocoLineMissed:          robot.JacocoLineMissed,
			JacocoBranchMissed:        robot.JacocoBranchMissed,
			JacocoInstructionMissed:   robot.JacocoInstructionMissed,
			EvoSuiteBranch:            robot.EvoSuiteBranch,
			EvoSuiteException:         robot.EvoSuiteException,
			EvoSuiteWeakMutation:      robot.EvoSuiteWeakMutation,
			EvoSuiteOutput:            robot.EvoSuiteOutput,
			EvoSuiteMethod:            robot.EvoSuiteMethod,
			EvoSuiteMethodNoException: robot.EvoSuiteMethodNoException,
			EvoSuiteCBranch:           robot.EvoSuiteCBranch,
		}
	}

	err := rs.db.
		Clauses(clause.OnConflict{
			UpdateAll: true,
		}).
		CreateInBatches(&robots, 100).
		Error

	// Recupero tutti i dati dalla tabella robots e stampo i risultati
	var robotsFromDB []model.Robot
	err = rs.db.Find(&robotsFromDB).Error
	if err != nil {
		log.Fatal(err)
	}

	log.Println("Dati dalla tabella robots:")
	for _, r := range robotsFromDB {
		log.Printf("ID: %d, TestClassId: %s, Difficulty: %s, Type: %d\n",
			r.ID, r.TestClassId, r.Difficulty, r.Type)
	}

	return len(robots), api.MakeServiceError(err)
}

func (gs *RobotStorage) FindByFilter(testClassId string, difficulty string, t RobotType) (Robot, error) {

	log.Println("robotType: ", t)
	log.Println("difficulty: ", difficulty)
	log.Println("robotType: ", t.AsInt8())

	var (
		robot model.Robot
		ids   []int64
	)

	err := gs.db.Transaction(func(tx *gorm.DB) error {
		query := tx.Model(&model.Robot{}).
			Where(&model.Robot{
				TestClassId: testClassId,
				Difficulty:  difficulty,
				Type:        t.AsInt8(),
			}).
			Where("type = ?", t.AsInt8())

		// Recupera solo gli ID
		if err := query.Select("id").Find(&ids).Error; err != nil {
			return err
		}

		if len(ids) == 0 {
			return gorm.ErrRecordNotFound
		}
		var id int64
		switch t {
		case evosuite:
			log.Println("ids: ", ids)
			id = ids[0]
		case randoop:
			log.Println("ids: ", ids)
			pos := rand.Intn(len(ids))
			id = ids[pos]
		default:
			return fmt.Errorf("%w: unsupported test engine", api.ErrInvalidParam)
		}

		return tx.First(&robot, id).Error
	})

	return *fromModel(&robot), api.MakeServiceError(err)
}

func (rs *RobotStorage) DeleteByTestClass(testClassId string) error {

	db := rs.db.Where(&model.Robot{TestClassId: testClassId}).
		Delete(&[]model.Robot{})
	if db.Error != nil {
		return db.Error
	} else if db.RowsAffected < 1 {
		return api.ErrNotFound
	}

	return nil
}
