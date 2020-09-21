package x7;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AppTest {

    @Autowired
    private XxxTest xxxTest;

    @Test
    public void testAll(){

        xxxTest.testFindToHandle();
        xxxTest.testTemporaryTable();
        xxxTest.inOrder();
        xxxTest.testOrderFind();
        xxxTest.testOrderFindByAlia();
        xxxTest.testNonPaged();
        xxxTest.testListCriteria();
        xxxTest.testResultMapped();
        xxxTest.testListPlainValue();
        xxxTest.testAlia();
        xxxTest.distinct();
        xxxTest.refreshByCondition();
        xxxTest.testSimple();

        xxxTest.testCriteria();

        {
//        xxxTest.testRefreshConditionRemote();
//        xxxTest.testCriteriaRemote();

//        xxxTest.testResultMappedRemote();
        }

        {
//        xxxTest.testOne();
//        xxxTest.testListCriteria();
//        xxxTest.refreshByCondition();
//        xxxTest.testListCriteria();
//        xxxTest.testRemove();
//        xxxTest.testListCriteria();
//        xxxTest.testCreate();
//        xxxTest.testListCriteria();
//        xxxTest.testCreateOrReplace();
//        xxxTest.testListCriteria();
//        xxxTest.testListCriteria();
//        xxxTest.create();
//        xxxTest.createBatch();
//        xxxTest.testRemove();
//        xxxTest.testRestTemplate();
//nullANDSUBANDIS_NOT_NULLcatFriendNameIS_NOT_NULLANDGTdogId1ANDIS_NOT_NULLtypeIS_NOT_NULL110idDESC
//nullANDSUBANDIS_NOT_NULLcatFriendNameIS_NOT_NULLANDGTdogId1ANDIS_NOT_NULLtypeIS_NOT_NULL110idDESC
        }


    }

}
