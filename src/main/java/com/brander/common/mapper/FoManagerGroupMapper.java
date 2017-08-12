package com.brander.common.mapper;

import com.brander.common.domain.FoManagerGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FoManagerGroupMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FoManagerGroup record);

    int insertSelective(FoManagerGroup record);

    FoManagerGroup selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FoManagerGroup record);

    int updateByPrimaryKey(FoManagerGroup record);

    //查询用户组列表 全部 或 search 查询
    List<FoManagerGroup> selectByList(@Param("search") String search);

}