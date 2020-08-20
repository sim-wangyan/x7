package x7.demo.remote;


import io.xream.sqli.core.builder.Criteria;
import io.xream.sqli.core.builder.condition.RefreshCondition;
import io.xream.x7.common.web.ViewEntity;
import x7.demo.bean.Cat;
import x7.demo.ro.CatRO;
import io.xream.x7.reyc.ReyClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


@ReyClient(value = "http://${web.demo}/xxx", circuitBreaker = "test", retry = true, fallback = TestFallback.class, groupRouter = CatServiceGroupRouterForK8S.class)
public interface TestServiceRemote {


    @RequestMapping(value = "/reyc/test")
    List<Cat> testFallBack(CatRO ro);

    @RequestMapping(value = "/time/test", method = RequestMethod.GET)
    Boolean testTimeJack();

    @RequestMapping(value = "/reyc/base", method = RequestMethod.GET)
    int getBase();

    @RequestMapping("/remote/criteria/test")
    ViewEntity testCriteriaRemote(Criteria criteria);

    @RequestMapping("/remote/resultmapped/test")
    ViewEntity testResultMappedRemote(Criteria.ResultMappedCriteria criteria);

    @RequestMapping("/remote/refreshCondition/test")
    ViewEntity testRefreshConditionnRemote( RefreshCondition refreshCondition);

    @RequestMapping("/refresh")
    ViewEntity refreshByCondition(Cat cat);


    @RequestMapping("/create")
    ViewEntity create();

    @RequestMapping("/get")
    ViewEntity get();

    @RequestMapping("/oneKey")
    ViewEntity testOneKey(Long keyOne);
}