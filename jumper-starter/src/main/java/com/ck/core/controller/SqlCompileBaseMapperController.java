package com.ck.core.controller;

import com.ck.api.TResult;
import com.ck.core.mybatis.SqlCompileBaseMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api(tags = "动态加载BaseMapper")
@RequestMapping("/jumperSqlCompile")
public class SqlCompileBaseMapperController {

    @Resource
    private SqlCompileBaseMapper sqlCompileBaseMapper;

    @ApiOperation(value = "Sql动态构建BaseMapper")
    @GetMapping("/registry")
    @ResponseBody
    public TResult<Boolean> registryMapper(@RequestParam(value = "className") String className, @RequestParam(value = "sql") String sql) {
        return TResult.ok(sqlCompileBaseMapper.registryBaseMapper(className, sql));
    }

    @ApiOperation(value = "重置Sql动态构建BaseMapper")
    @GetMapping("/reset")
    @ResponseBody
    public TResult<Boolean> resetMapper(@RequestParam(value = "className") String className, @RequestParam(value = "sql") String sql) {
        return TResult.ok(sqlCompileBaseMapper.resetBaseMapper(className, sql));
    }

    @ApiOperation(value = "卸载BaseMapper")
    @GetMapping("/destroy")
    @ResponseBody
    public TResult<Boolean> destroyMapper(@RequestParam(value = "className") String className) {
        return TResult.ok(sqlCompileBaseMapper.destroyBaseMapper(className));
    }
}
