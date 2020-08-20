package x7.demo.service;

import io.xream.sqli.core.builder.Criteria;
import io.xream.sqli.core.builder.RowHandler;
import io.xream.sqli.core.builder.condition.RefreshCondition;
import x7.demo.bean.Cat;

import java.util.Map;

public interface CatService {

    boolean refresh(RefreshCondition<Cat> refreshCondition);

    void findToHandle(Criteria.ResultMappedCriteria resultMappedCriteria, RowHandler<Map<String,Object>> rowHandler);

    void findToHandleC(Criteria criteria, RowHandler<Cat> rowHandler);
}