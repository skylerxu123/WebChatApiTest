/**
 * projectName: WeChatWorkApiTest
 * fileName: Demo_01.java
 * packageName: com.wechat.testcase
 * date: 2020-07-18 2:49 下午
 */
package com.wechat.testcase;

import com.wechat.apiobject.DepartmentApiObject;
import com.wechat.apiobject.TokenHelper;
import com.wechat.task.EvnTask;
import com.wechat.utils.FakerUtils;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 优化记录：
 * 1、增加了入参实时获取的逻辑
 * 2、增加了脚本的独立性改造
 * 3、通过添加evnClear方法解决脚本无法重复运行的问题
 * 4、对脚本行了分层，减少了重复代码，减少了很多维护成本
 * 5、以数据驱动的方式将入参数据从代码剥离。
 * 6、使用Junit5提供的Java 8 lambdas的断言方法，增加了脚本的容错性。
 **/
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Demo_06_01 {
    private static final Logger logger = LoggerFactory.getLogger(Demo_06_01.class);

    static String accessToken;
    static String departmentId;

    @BeforeAll
    static void getAccessToken() {
        accessToken = TokenHelper.getAccessToken();
    }

    @BeforeEach
    @AfterEach
    void evnClear() {
        EvnTask.evnClear(accessToken);
    }

    @CsvFileSource(resources = "/data/createDepartment.csv", numLinesToSkip = 1)
    @ParameterizedTest
    @Description("创建部门")
    void creatDepartment(String creatName, String creatNameEn, String returnCode) {
        Response creatResponse = DepartmentApiObject.creatDepartMent(creatName, creatNameEn, accessToken);
        departmentId = creatResponse.path("id") != null ? creatResponse.path("id").toString() : null;
        assertEquals(returnCode, creatResponse.path("errcode").toString());
    }

    @Test
    @Description("更新部门")
    void updateDepartment() {
        Response creatResponse = DepartmentApiObject.creatDepartMent(accessToken);
        departmentId = creatResponse.path("id") != null ? creatResponse.path("id").toString() : null;

        String updateName = "creatName" + FakerUtils.getTimeStamp();
        String updateNameEn = "creatNamEn" + FakerUtils.getTimeStamp();

        Response updateResponse = DepartmentApiObject.updateDepMent(updateName, updateNameEn, departmentId, accessToken);
        assertEquals("0", updateResponse.path("errcode").toString());

    }

    @Test
    @Description("查询部门")
    void listDepartment() {
        String creatName = "creatNam" + FakerUtils.getTimeStamp();
        String creatNameEn = "creatNamEn" + FakerUtils.getTimeStamp();

        Response creatResponse = DepartmentApiObject.creatDepartMent(creatName, creatNameEn, accessToken);
        departmentId = creatResponse.path("id") != null ? creatResponse.path("id").toString() : null;

        Response listResponse = DepartmentApiObject.listDepartMent(departmentId, accessToken);

        assertAll("返回值校验!",
                () -> assertEquals("0" + "x", listResponse.path("errcode").toString()),
                () -> assertEquals(departmentId + "x", listResponse.path("department.id[0]").toString()),
                () -> assertEquals(creatName + "x", listResponse.path("department.name[0]").toString()),
                () -> assertEquals(creatNameEn + "x", listResponse.path("department.name_en[0]").toString())
        );
//


    }

    @Test
    @Description("删除部门")
    void deletDepartment() {
        Response creatResponse = DepartmentApiObject.creatDepartMent(accessToken);
        departmentId = creatResponse.path("id") != null ? creatResponse.path("id").toString() : null;

        Response deleteResponse = DepartmentApiObject.deleteDepartMent(departmentId, accessToken);
        assertEquals("0", deleteResponse.path("errcode").toString());
    }
}