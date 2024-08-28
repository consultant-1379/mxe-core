package com.ericsson.mxe.jcat.test;

import com.ericsson.mxe.jcat.driver.cli.MxeCliDriver;
import com.ericsson.mxe.jcat.driver.keycloak.KeycloakDriver.RolePermission;
import com.ericsson.mxe.jcat.driver.keycloak.KeycloakDriver.RoleType;
import com.ericsson.mxe.jcat.driver.util.DriverFactory;
import com.ericsson.mxe.jcat.dto.MxeUser;
import com.ericsson.mxe.jcat.util.AccessControlUtil;
import io.kubernetes.client.openapi.ApiException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import se.ericsson.jcat.fw.annotations.JcatClass;
import se.ericsson.jcat.fw.annotations.JcatMethod;
import java.io.IOException;
import java.util.Arrays;
import static com.ericsson.mxe.jcat.test.MxeModelTestHelper.*;
import static com.ericsson.mxe.jcat.test.MxeServiceTestHelper.*;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.ERROR_RESOURCE_RELEASE;
import static com.ericsson.mxe.jcat.test.MxeTestHelper.STATUS_AVAILABLE;

/**
 * @JcatDocChapterDescription Chapter covering performance tests.
 */
@JcatClass(chapterName = "Performance Tests")
public class MxeAccessControlTest extends MxeKubernetesTestBase {
    private static final String TEST_USER = "acl-test-user";
    private static final String TEST_USER_SECOND = "acl-test-user-second";
    private static final String TEST_USER_PASSWORD = "password";
    private static final String TEST_ROLE_ALL = "mxe_acl_test_role_all";
    private static final String TEST_ROLE_READ = "mxe_acl_test_role_read";
    private static final String TEST_SECOND_ROLE_ALL = "mxe_acl_test_second_role_all";
    private static final String TEST_SECOND_ROLE_READ = "mxe_acl_test_second_role_read";
    private static final String TEST_GROUP = "acl_test_group";
    private static final String TEST_GROUP_SECOND = "acl_test_group_second";
    private static final String TEST_DOMAIN = "com.ericsson";
    private static final String TEST_DOMAIN_SECOND = "se.nemericsson";
    private static final String MXE_MODEL_SERVICE_NAME = "test-model-service";

    // MXE 2.0: IAM rbac policy retricts access. Adding 'mxe_model_serving_role' to enable access to
    // models/model-services
    private static final String MXE_MODEL_SERVING_ROLE = "mxe_model_serving_role";

    private MxeUser mxeUser;
    private String cluster;

    @JcatMethod(testTag = "ACCESS-CONTROL-TEST-SETUP", testTitle = "Access control configuration")
    @BeforeTest
    @Parameters({"cluster"})
    public void setupForAccessControl(String cluster) throws IOException, ApiException {
        this.cluster = cluster;
        this.mxeUser = getCurrentUserStep(cluster);
    }

    /**
     * @JcatTcDescription Basic access control settings
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about setting basic access control
     * @JcatTcAction Create a user in keycloak
     * @JcatTcActionResult User created
     * @JcatTcAction Create role for test domain with read right for models and add it to user
     * @JcatTcActionResult Role created and added to user
     * @JcatTcAction Revoke role from user and delete role and user
     * @JcatTcActionResult Role and user deleted
     * @JcatTcAction Create a user in keycloak
     * @JcatTcActionResult User created
     * @JcatTcAction Create group and add to user
     * @JcatTcActionResult Group created and added to user
     * @JcatTcAction Create role for test domain with all right for models-services and add it to group
     * @JcatTcActionResult Role created and added to group
     * @JcatTcAction Revoke role from group, remove user from group and delete group, role and user
     * @JcatTcActionResult Role, grup and user deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "BASIC-ACCESS-CONTROL", testTitle = "Test of setting up basic access control")
    public void basicAccessControlTest() throws IOException {
        setupAccessControlInStep(TEST_USER, TEST_USER_PASSWORD, TEST_ROLE_READ, TEST_DOMAIN, RolePermission.READ,
                RoleType.MODEL);
        deleteAccessControlInStep(TEST_USER, TEST_ROLE_READ);

        setupAccessControlInStep(TEST_USER, TEST_USER_PASSWORD, TEST_GROUP, TEST_ROLE_ALL, TEST_DOMAIN_SECOND,
                RolePermission.ALL, RoleType.MODEL_SERVICE);
        deleteAccessControlInStep(TEST_USER, TEST_GROUP, TEST_ROLE_ALL);
    }

    /**
     * @JcatTcDescription CLI user change test
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about changing CLI user
     * @JcatTcAction Create new user in keycloak
     * @JcatTcActionResult User created
     * @JcatTcAction Relogin with new user
     * @JcatTcActionResult New user logged in
     * @JcatTcAction Delete user
     * @JcatTcActionResult User deleted
     * @JcatTcPostconditions NA
     */
    @Test
    @JcatMethod(testTag = "MXE-USER-TEST", testTitle = "Test of getting MXE user")
    @Parameters({"newUserName", "newPassword"})
    public void mxeUserTest(String newUserName, String newPassword) throws IOException {
        setTestStepBegin("Create user");
        assertTrue("Failed to create user " + newUserName,
                keycloakDriver.createUserWithRole(newUserName, newPassword, MXE_MODEL_SERVING_ROLE).isPresent());

        reloginWithUser(newUserName, newPassword);

        MxeUser mxeUser = getCurrentUserStep(cluster);
        saveAssertEquals("Current user should be " + newUserName + " but it is " + mxeUser.getUserName(), newUserName,
                mxeUser.getUserName());

        setTestStepBegin("Delete user");
        saveAssertTrue("Failed to delete user " + newUserName, keycloakDriver.deleteUser(newUserName));
    }

    private MxeUser getCurrentUserStep(String cluster) throws IOException {
        setTestStepBegin("Getting current MXE user");
        setSubTestStep("Getting user name");
        MxeUser mxeUser = AccessControlUtil.getCurrentUser(mxeCluster, cluster);
        assertNotNull("Not found MXE user on cluster " + cluster, mxeUser.getUserName());
        setSubTestStep("Found user name");
        setTestInfo("Current MXE user name on cluster " + cluster + ": " + mxeUser.getUserName());
        return mxeUser;
    }

    /**
     * @JcatTcDescription Testing model access control with users with roles
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about testing model access control with users with roles
     * @JcatTcAction Create first user with role to have all access in first test domain
     * @JcatTcActionResult User and role created
     * @JcatTcAction Create second user with role to have all access in second test domain
     * @JcatTcActionResult User and role created
     * @JcatTcAction Login with first user
     * @JcatTcActionResult First user logged in
     * @JcatTcAction Onboard model with first user in first domain, user has full access rights in first domain
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Login with second user
     * @JcatTcActionResult Second user logged in
     * @JcatTcAction Onboard model with second user in second domain, user has full access rights in second domain
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Delete model from second domain with second user, who has full access there
     * @JcatTcActionResult Model deleted
     * @JcatTcAction List invisible model in first domain with second user who has no permissions there
     * @JcatTcActionResult Models in first domain are not visible for second user
     * @JcatTcAction Try to onboard model in first domain with second user who has no permissions there
     * @JcatTcActionResult Failed to onboard model
     * @JcatTcAction Add read permission for the first domain to second user
     * @JcatTcActionResult Read rights added
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction List model in first domain with second user who has read permissions there
     * @JcatTcActionResult Models in first domain are visible for second user
     * @JcatTcAction Try to onboard model without permission with second user who has read rights there
     * @JcatTcActionResult Failed to onboard model
     * @JcatTcAction Add all permission for the first dommain to second user
     * @JcatTcActionResult All permission added
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Onboard model in first domain with second user who has full rights in first domain
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Delete model in first domain with second user who has full rights in first domain
     * @JcatTcActionResult Model successfully deleted
     * @JcatTcAction Login with first user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Delete models and services from first domain
     * @JcatTcActionResult Models and services deleted successfully
     * @JcatTcAction Delete users and roles
     * @JcatTcActionResult Users and roles are successfully removed
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "secondPackageName", "secondModelId", "secondModelVersion"})
    @JcatMethod(testTag = "MODEL-ACCESS-CONTROL-ROLES",
            testTitle = "Testing model access control with users with roles")
    public void modelAccessControlTestWithRoles(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion) throws IOException {
        accessControlModelTest(packageName, modelId, modelVersion, secondPackageName, secondModelId, secondModelVersion,
                false);
    }

    /**
     * @JcatTcDescription Testing model access control with users with group
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about testing model access control with users with group
     * @JcatTcAction Create first user with group to have all access in first test domain
     * @JcatTcActionResult User, group and role created
     * @JcatTcAction Create second user with group to have all access in second test domain
     * @JcatTcActionResult User,group and role created
     * @JcatTcAction Login with first user
     * @JcatTcActionResult First user logged in
     * @JcatTcAction Onboard model with first user in first domain, user has full access rights in first domain
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Login with second user
     * @JcatTcActionResult Second user logged in
     * @JcatTcAction Onboard model with second user in second domain, user has full access rights in second domain
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Delete model from second domain with second user, who has full access there
     * @JcatTcActionResult Model deleted
     * @JcatTcAction List invisible model in first domain with second user who has no permissions there
     * @JcatTcActionResult Models in first domain are not visible for second user
     * @JcatTcAction Try to onboard model in first domain with second user who has no permissions there
     * @JcatTcActionResult Failed to onboard model
     * @JcatTcAction Add read permission for the first domain to second user
     * @JcatTcActionResult Read rights added
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction List model in first domain with second user who has read permissions there
     * @JcatTcActionResult Models in first domain are visible for second user
     * @JcatTcAction Try to onboard model without permission with second user who has read rights there
     * @JcatTcActionResult Failed to onboard model
     * @JcatTcAction Add all permission for the first dommain to second user
     * @JcatTcActionResult All permission added
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Onboard model in first domain with second user who has full rights in first domain
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Delete model in first domain with second user who has full rights in first domain
     * @JcatTcActionResult Model successfully deleted
     * @JcatTcAction Login with first user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Delete models and services from first domain
     * @JcatTcActionResult Models and services deleted successfully
     * @JcatTcAction Delete users, roles and groups
     * @JcatTcActionResult Users, group and roles are successfully removed
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "secondPackageName", "secondModelId", "secondModelVersion"})
    @JcatMethod(testTag = "MODEL-ACCESS-CONTROL-GROUPS",
            testTitle = "Testing  model access control with users with groups")
    public void modelAccessControlTestWithGroups(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion) throws IOException {
        accessControlModelTest(packageName, modelId, modelVersion, secondPackageName, secondModelId, secondModelVersion,
                true);
    }

    /**
     * @JcatTcDescription Testing model access control with users with roles
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about testing model access control with users with roles
     * @JcatTcAction Create first user with role to have all access in first test domain
     * @JcatTcActionResult User and role created
     * @JcatTcAction Create second user with role to have all access in second test domain
     * @JcatTcActionResult User and role created
     * @JcatTcAction Login with first user
     * @JcatTcActionResult First user logged in
     * @JcatTcAction Onboard model with first user in first domain, user has full access rights in first domain
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Create service with first user, who has full access rights in first domain
     * @JcatTcActionResult Service created
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Onboard model in own second domain with second user, who has full access there
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Create service in own domain with second user, who has full access there
     * @JcatTcActionResult Service successfully created
     * @JcatTcAction List invisible service in first domain with second user with second user who has no permissions
     *               there
     * @JcatTcActionResult Service in first domain is not visible for second user
     * @JcatTcAction Try to create service in first domain with model from first domain, with second user who has no
     *               permissions in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to create service in first domain with model from second domain, with second user who has no
     *               permissions in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to create service in own second domain with model from first domain, with second user who has
     *               no permissions in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to modify model service in first domain with model from first domain, with second user who has
     *               no permissions in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Try to modify model service in first domain with model from second domain, with second user who has
     *               no permissions in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Try to modify model service in second domain with model from first domain, with second user who has
     *               no permissions in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Add read permission for the first domain to second user
     * @JcatTcActionResult Read permission added
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction List services of first domain with second user who has read rights there
     * @JcatTcActionResult Services in first domain are visible for second user
     * @JcatTcAction Try to create service in first domain with model from first domain, with second user who has read
     *               rights in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to create service in first domain with model from second domain, with second user who has read
     *               rights in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to create service in own second domain with model from first domain, with second user who has
     *               read rights in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Delete new service from second domain
     * @JcatTcActionResult Service deleted
     * @JcatTcAction Try to modify model service in first domain with model from first domain, with second user who has
     *               read rights in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Try to modify model service in first domain with model from second domain, with second user who has
     *               read rights in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Try to modify model service in second domain with model from first domain, with second user who has
     *               read rights in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Add all permission for the first domain to second user
     * @JcatTcActionResult All permission added
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Create service in first domain with second user who has full rights in first domain
     * @JcatTcActionResult Service successfully created
     * @JcatTcAction Delete service from first domain
     * @JcatTcActionResult Service successfully deleted
     * @JcatTcAction Try to create service in own second domain with model from first domain, with second user who has
     *               full rights in first domain
     * @JcatTcActionResult Service successfully deleted
     * @JcatTcAction Delete service from second domain
     * @JcatTcActionResult Service successfully deleted
     * @JcatTcAction Try to modify model service in second domain with model from first domain, with second user who has
     *               full rights in first domain
     * @JcatTcActionResult Service successfully modified
     * @JcatTcAction Try to modify model service in second domain with model from second domain, with second user who
     *               has full rights in first domain
     * @JcatTcActionResult Service successfully modified
     * @JcatTcAction Try to modify model service in first domain with model from second domain, with second user who has
     *               full rights in first domain
     * @JcatTcActionResult Service successfully modified
     * @JcatTcAction Try to modify model service in first domain with model from first domain, with second user who has
     *               full rights in first domain
     * @JcatTcActionResult Service successfully modified
     * @JcatTcAction Delete service and model from second domain with second user, who has full access there
     * @JcatTcActionResult Models and services successfully deleted
     * @JcatTcAction Login with first user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Delete models and services from first domain
     * @JcatTcActionResult Models and services deleted successfully
     * @JcatTcAction Delete users and roles
     * @JcatTcActionResult Users and roles are successfully removed
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "secondPackageName", "secondModelId", "secondModelVersion"})
    @JcatMethod(testTag = "SERVICE-ACCESS-CONTROL-ROLES",
            testTitle = "Testing model service access control with users with roles")
    public void serviceAccessControlTestWithRoles(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion) throws IOException {
        accessControlServiceTest(packageName, modelId, modelVersion, secondPackageName, secondModelId,
                secondModelVersion, false);
    }

    /**
     * @JcatTcDescription Testing model access control with users with group
     * @JcatTcPreconditions MXE cluster is up and running
     * @JcatTcInstruction The testcase is about testing model access control with users with group
     * @JcatTcAction Create first user with group to have all access in first test domain
     * @JcatTcActionResult User, group and role created
     * @JcatTcAction Create second user with group to have all access in second test domain
     * @JcatTcActionResult User,group and role created
     * @JcatTcAction Login with first user
     * @JcatTcActionResult First user logged in
     * @JcatTcAction Onboard model with first user in first domain, user has full access rights in first domain
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Create service with first user, who has full access rights in first domain
     * @JcatTcActionResult Service created
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Onboard model in own second domain with second user, who has full access there
     * @JcatTcActionResult Model successfully onboarded
     * @JcatTcAction Create service in own domain with second user, who has full access there
     * @JcatTcActionResult Service successfully created
     * @JcatTcAction List invisible service in first domain with second user with second user who has no permissions
     *               there
     * @JcatTcActionResult Service in first domain is not visible for second user
     * @JcatTcAction Try to create service in first domain with model from first domain, with second user who has no
     *               permissions in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to create service in first domain with model from second domain, with second user who has no
     *               permissions in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to create service in own second domain with model from first domain, with second user who has
     *               no permissions in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to modify model service in first domain with model from first domain, with second user who has
     *               no permissions in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Try to modify model service in first domain with model from second domain, with second user who has
     *               no permissions in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Try to modify model service in second domain with model from first domain, with second user who has
     *               no permissions in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Add read permission for the first domain to second user
     * @JcatTcActionResult Read permission added
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction List services of first domain with second user who has read rights there
     * @JcatTcActionResult Services in first domain are visible for second user
     * @JcatTcAction Try to create service in first domain with model from first domain, with second user who has read
     *               rights in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to create service in first domain with model from second domain, with second user who has read
     *               rights in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to create service in own second domain with model from first domain, with second user who has
     *               read rights in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Try to modify model service in first domain with model from first domain, with second user who has
     *               read rights in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Try to modify model service in first domain with model from second domain, with second user who has
     *               read rights in first domain
     * @JcatTcActionResult Failed to modify service
     * @JcatTcAction Try to modify model service in second domain with model from first domain, with second user who has
     *               read rights in first domain
     * @JcatTcActionResult Failed to create service
     * @JcatTcAction Add all permission for the first domain to second user
     * @JcatTcActionResult All permission added
     * @JcatTcAction Login with second user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Create service in first domain with second user who has full rights in first domain
     * @JcatTcActionResult Service successfully created
     * @JcatTcAction Delete service from first domain
     * @JcatTcActionResult Service successfully deleted
     * @JcatTcAction Try to create service in own second domain with model from first domain, with second user who has
     *               full rights in first domain
     * @JcatTcActionResult Service successfully deleted
     * @JcatTcAction Delete service from second domain
     * @JcatTcActionResult Service successfully deleted
     * @JcatTcAction Try to modify model service in second domain with model from first domain, with second user who has
     *               full rights in first domain
     * @JcatTcActionResult Service successfully modified
     * @JcatTcAction Try to modify model service in second domain with model from second domain, with second user who
     *               has full rights in first domain
     * @JcatTcActionResult Service successfully modified
     * @JcatTcAction Try to modify model service in first domain with model from second domain, with second user who has
     *               full rights in first domain
     * @JcatTcActionResult Service successfully modified
     * @JcatTcAction Try to modify model service in first domain with model from first domain, with second user who has
     *               full rights in first domain
     * @JcatTcActionResult Service successfully modified
     * @JcatTcAction Delete service and model from second domain with second user, who has full access there
     * @JcatTcActionResult Models and services successfully deleted
     * @JcatTcAction Login with first user
     * @JcatTcActionResult User logged in
     * @JcatTcAction Delete models and services from first domain
     * @JcatTcActionResult Models and services deleted successfully
     * @JcatTcAction Delete users, roles and groups
     * @JcatTcActionResult Users, group and roles are successfully removed
     */
    @Test
    @Parameters({"packageName", "modelId", "modelVersion", "secondPackageName", "secondModelId", "secondModelVersion"})
    @JcatMethod(testTag = "SERVICE-ACCESS-CONTROL-GROUPS",
            testTitle = "Testing model service access control with users with groups")
    public void serviceAccessControlTestWithGroups(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion) throws IOException {
        accessControlServiceTest(packageName, modelId, modelVersion, secondPackageName, secondModelId,
                secondModelVersion, true);
    }

    private void accessControlModelTest(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion, boolean testWithGroup) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            if (testWithGroup) {
                // Create users with groups and roles
                setupAccessControlInStep(TEST_USER, TEST_USER_PASSWORD, TEST_GROUP, TEST_ROLE_ALL, TEST_DOMAIN,
                        RolePermission.ALL, RoleType.ALL);
                setupAccessControlInStep(TEST_USER_SECOND, TEST_USER_PASSWORD, TEST_GROUP_SECOND, TEST_SECOND_ROLE_ALL,
                        TEST_DOMAIN_SECOND, RolePermission.ALL, RoleType.ALL);
            } else {
                // Create users with roles
                setupAccessControlInStep(TEST_USER, TEST_USER_PASSWORD, TEST_ROLE_ALL, TEST_DOMAIN, RolePermission.ALL,
                        RoleType.ALL);
                setupAccessControlInStep(TEST_USER_SECOND, TEST_USER_PASSWORD, TEST_SECOND_ROLE_ALL, TEST_DOMAIN_SECOND,
                        RolePermission.ALL, RoleType.ALL);
            }
            // Login with first user
            reloginWithUser(TEST_USER, TEST_USER_PASSWORD);

            // Onboard model with first user, who has full access rights in first domain
            modelOnboard(mxeCliDriver,
                    "Successful onboard of model in first domain with first user, who has full access rights in first domain",
                    String.join(".", TEST_DOMAIN, modelId), modelVersion, packageName);
            waitUntilModelStatusIs(mxeCliDriver, String.join(".", TEST_DOMAIN, modelId), modelVersion, STATUS_AVAILABLE,
                    MODEL_ONBOARD_TIMEOUT);

            // Login with second user
            reloginWithUser(TEST_USER_SECOND, TEST_USER_PASSWORD);

            // Onboard model in own second domain with second user, who has full access there
            modelOnboard(mxeCliDriver,
                    "Successful onboard of model in second domain with second user, who has full access rights in second domain",
                    String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion, secondPackageName);
            waitUntilModelStatusIs(mxeCliDriver, String.join(".", TEST_DOMAIN_SECOND, secondModelId),
                    secondModelVersion, STATUS_AVAILABLE, MODEL_ONBOARD_TIMEOUT);
            // Delete model from second domain with second user, who has full access there
            modelDelete(mxeCliDriver, String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion);

            // List invisible model in first domain with second user who has no permissions there
            checkModelNotFound(mxeCliDriver,
                    "List invisible model in first domain with second user who has no permissions in first domain",
                    String.join(".", TEST_DOMAIN, modelId), modelVersion);

            // Try to onboard model without permission with second user who has no permissions there
            modelOnboardExpectFail(mxeCliDriver,
                    "Failed model onboard in first domain with second user who has no permissions in first domain",
                    String.join(".", TEST_DOMAIN, secondModelId), secondModelVersion, secondPackageName,
                    "Error: There is no permission to onboard model " + String.join(".", TEST_DOMAIN, secondModelId));


            // Add read permission for the first domain to second user
            if (testWithGroup) {
                createRoleAndAddToGroup(TEST_GROUP_SECOND, TEST_ROLE_READ, TEST_DOMAIN, RolePermission.READ,
                        RoleType.ALL, true);
            } else {
                createRoleAndAddToUser(TEST_USER_SECOND, TEST_ROLE_READ, TEST_DOMAIN, RolePermission.READ, RoleType.ALL,
                        true);
            }

            // Login with second user
            reloginWithUser(TEST_USER_SECOND, TEST_USER_PASSWORD);

            // List models of first domain wih second user
            waitUntilModelStatusIs(mxeCliDriver, String.join(".", TEST_DOMAIN, modelId), modelVersion, STATUS_AVAILABLE,
                    MODEL_ONBOARD_TIMEOUT);

            // Try to onboard model without permission with second user who has read rights there
            modelOnboardExpectFail(mxeCliDriver,
                    "Failed model onboard in first domain with second user who has only read rights in first domain",
                    String.join(".", TEST_DOMAIN, secondModelId), secondModelVersion, secondPackageName,
                    "Error: There is no permission to onboard model " + String.join(".", TEST_DOMAIN, secondModelId));

            // Add all permission for the first dommain to second user
            if (testWithGroup) {
                addRoleToGroup(TEST_ROLE_ALL, TEST_GROUP_SECOND, true, false);
            } else {
                addRoleToUser(TEST_ROLE_ALL, TEST_USER_SECOND, true, false);
            }

            // Login with second user
            reloginWithUser(TEST_USER_SECOND, TEST_USER_PASSWORD);

            // Onboard model in first domain with second user who has full rights in first domain
            modelOnboard(mxeCliDriver,
                    "Successful onboard of model in first domain with second user, who got full access rights in first domain",
                    String.join(".", TEST_DOMAIN, secondModelId), secondModelVersion, secondPackageName);
            waitUntilModelStatusIs(mxeCliDriver, String.join(".", TEST_DOMAIN, secondModelId), secondModelVersion,
                    STATUS_AVAILABLE, MODEL_ONBOARD_TIMEOUT);
            modelDelete(mxeCliDriver, String.join(".", TEST_DOMAIN, secondModelId), secondModelVersion);

            // Login with first user
            reloginWithUser(TEST_USER, TEST_USER_PASSWORD);

            // Delete model from first domain
            modelDelete(mxeCliDriver, String.join(".", TEST_DOMAIN, modelId), modelVersion);

            // Delete users and roles
            if (testWithGroup) {
                revokeRoleFromGroupAndDelete(TEST_ROLE_READ, TEST_GROUP_SECOND, true, false);
                deleteAccessControlInStep(TEST_USER_SECOND, TEST_GROUP_SECOND, TEST_SECOND_ROLE_ALL);
                deleteAccessControlInStep(TEST_USER, TEST_GROUP, TEST_ROLE_ALL);
            } else {
                revokeRoleFromUserAndDelete(TEST_ROLE_READ, TEST_USER_SECOND, true);
                deleteAccessControlInStep(TEST_USER_SECOND, TEST_SECOND_ROLE_ALL);
                deleteAccessControlInStep(TEST_USER, TEST_ROLE_ALL);
            }

            // Login with original
            reloginWithUser(mxeUser.getUserName(), mxeUser.getPassword());

        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    private void accessControlServiceTest(String packageName, String modelId, String modelVersion,
            String secondPackageName, String secondModelId, String secondModelVersion, boolean testWithGroup) {
        try (final MxeCliDriver mxeCliDriver = DriverFactory.getMxeCliDriver(testExecutionHost)) {
            if (testWithGroup) {
                // Create users with groups and roles
                setupAccessControlInStep(TEST_USER, TEST_USER_PASSWORD, TEST_GROUP, TEST_ROLE_ALL, TEST_DOMAIN,
                        RolePermission.ALL, RoleType.ALL);
                setupAccessControlInStep(TEST_USER_SECOND, TEST_USER_PASSWORD, TEST_GROUP_SECOND, TEST_SECOND_ROLE_ALL,
                        TEST_DOMAIN_SECOND, RolePermission.ALL, RoleType.ALL);
            } else {
                // Create users with roles
                setupAccessControlInStep(TEST_USER, TEST_USER_PASSWORD, TEST_ROLE_ALL, TEST_DOMAIN, RolePermission.ALL,
                        RoleType.ALL);
                setupAccessControlInStep(TEST_USER_SECOND, TEST_USER_PASSWORD, TEST_SECOND_ROLE_ALL, TEST_DOMAIN_SECOND,
                        RolePermission.ALL, RoleType.ALL);
            }
            // Login with first user
            reloginWithUser(TEST_USER, TEST_USER_PASSWORD);

            // Onboard model with first user, who has full access rights in first domain
            modelOnboard(mxeCliDriver,
                    "Successful onboard of model in first domain with first user, who has full access rights in first domain",
                    String.join(".", TEST_DOMAIN, modelId), modelVersion, packageName);
            waitUntilModelStatusIs(mxeCliDriver, String.join(".", TEST_DOMAIN, modelId), modelVersion, STATUS_AVAILABLE,
                    MODEL_ONBOARD_TIMEOUT);

            // Create service with first user, who has full access rights in first domain
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME, String.join(".", TEST_DOMAIN, modelId),
                    modelVersion, TEST_DOMAIN, 1);
            serviceListInStep(mxeCliDriver);
            verifyServiceDomain(mxeCliDriver, MXE_MODEL_SERVICE_NAME, TEST_DOMAIN);

            // Login with second user
            reloginWithUser(TEST_USER_SECOND, TEST_USER_PASSWORD);

            // Onboard model in own second domain with second user, who has full access there
            modelOnboard(mxeCliDriver,
                    "Successful onboard of model in second domain with second user, who has full access rights in second domain",
                    String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion, secondPackageName);
            waitUntilModelStatusIs(mxeCliDriver, String.join(".", TEST_DOMAIN_SECOND, secondModelId),
                    secondModelVersion, STATUS_AVAILABLE, MODEL_ONBOARD_TIMEOUT);

            // Create service in own domain with second user, who has full access there
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-second",
                    String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion, TEST_DOMAIN_SECOND, 1);
            serviceListInStep(mxeCliDriver);
            verifyServiceDomain(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-second", TEST_DOMAIN_SECOND);

            // List invisible service with second user with second user who has no permissions there
            checkServiceNotFound(mxeCliDriver,
                    "List invisible service with second user with second user who has no permissions in first domain",
                    MXE_MODEL_SERVICE_NAME);

            // Try to create service in first domain with model from first domain, with second user who has no
            // permissions in first domain
            serviceCreateSingleServiceExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third",
                    String.join(".", TEST_DOMAIN, modelId), modelVersion, TEST_DOMAIN, 1,
                    "Error: There is no permission to create model service " + MXE_MODEL_SERVICE_NAME
                            + "-third in domain " + TEST_DOMAIN);

            // Try to create service in first domain with model from second domain, with second user who has no
            // permissions
            // in first domain
            serviceCreateSingleServiceExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third",
                    String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion, TEST_DOMAIN, 1,
                    "Error: There is no permission to create model service " + MXE_MODEL_SERVICE_NAME
                            + "-third in domain " + TEST_DOMAIN);

            // Try to create service in own second domain with model from first domain, with second user who has no
            // permissions in first domain
            serviceCreateSingleServiceExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third",
                    String.join(".", TEST_DOMAIN, modelId), modelVersion, TEST_DOMAIN_SECOND, 1,
                    "Error: Model with ID \"" + String.join(".", TEST_DOMAIN, modelId) + "\" and version \""
                            + modelVersion + "\" does not exist, or there is no permission to use it");

            // try to modify model service in first domain with model from first domain, with second user who has no
            // permissions in first domain
            serviceModifyModelFailedInStep(mxeCliDriver,
                    "Failed to modify model service in first domain with model from first domain, with second user who has no permissions in first domain",
                    MXE_MODEL_SERVICE_NAME,
                    Arrays.asList(String.join(":", String.join(".", TEST_DOMAIN, modelId), modelVersion)),
                    "Error: There is no permission to modify model service " + MXE_MODEL_SERVICE_NAME);

            // try to modify model service in first domain with model from second domain, with second user who has no
            // permissions in first domain
            serviceModifyModelFailedInStep(mxeCliDriver,
                    "Failed to modify model service in first domain with model from second domain, with second user who has no permissions in first domain",
                    MXE_MODEL_SERVICE_NAME,
                    Arrays.asList(
                            String.join(":", String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion)),
                    "Error: There is no permission to modify model service " + MXE_MODEL_SERVICE_NAME);

            // try to modify model service in second domain with model from first domain, with second user who has no
            // permissions in first domain
            serviceModifyModelFailedInStep(mxeCliDriver,
                    "Failed to modify model service in second domain with model from first domain, with second user who has no permissions in first domain",
                    MXE_MODEL_SERVICE_NAME + "-second",
                    Arrays.asList(String.join(":", String.join(".", TEST_DOMAIN, modelId), modelVersion)),
                    "Error: Model with ID \"" + String.join(".", TEST_DOMAIN, modelId) + "\" and version \""
                            + modelVersion + "\" does not exist, or there is no permission to use it");

            // Add read permission for the first domain to second user
            if (testWithGroup) {
                createRoleAndAddToGroup(TEST_GROUP_SECOND, TEST_ROLE_READ, TEST_DOMAIN, RolePermission.READ,
                        RoleType.ALL, true);
            } else {
                createRoleAndAddToUser(TEST_USER_SECOND, TEST_ROLE_READ, TEST_DOMAIN, RolePermission.READ, RoleType.ALL,
                        true);
            }

            // Login with second user
            reloginWithUser(TEST_USER_SECOND, TEST_USER_PASSWORD);

            // List services of first domain with second user who has read rights there
            verifyServiceDomain(mxeCliDriver, MXE_MODEL_SERVICE_NAME, TEST_DOMAIN);

            // Try to create service in first domain with model from first domain, with second user who has read rights
            // in first domain
            serviceCreateSingleServiceExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third",
                    String.join(".", TEST_DOMAIN, secondModelId), secondModelVersion, TEST_DOMAIN, 1,
                    "Error: There is no permission to create model service " + MXE_MODEL_SERVICE_NAME
                            + "-third in domain " + TEST_DOMAIN);

            // Try to create service in first domain with model from second domain, with second user who has read rights
            // in first domain
            serviceCreateSingleServiceExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third",
                    String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion, TEST_DOMAIN, 1,
                    "Error: There is no permission to create model service " + MXE_MODEL_SERVICE_NAME
                            + "-third in domain " + TEST_DOMAIN);

            // Try to create service in own second domain with model from first domain, with second user who has read
            // rights in first domain
            serviceCreateSingleServiceExpectFail(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third",
                    String.join(".", TEST_DOMAIN, modelId), modelVersion, TEST_DOMAIN_SECOND, 1,
                    "Error: Model with ID \"" + String.join(".", TEST_DOMAIN, modelId) + "\" and version \""
                            + modelVersion + "\" does not exist, or there is no permission to use it");

            // try to modify model service in first domain with model from first domain, with second user who has read
            // rights in first domain
            serviceModifyModelFailedInStep(mxeCliDriver,
                    "Failed to modify model service in first domain with model from first domain, with second user who has read rights in first domain",
                    MXE_MODEL_SERVICE_NAME,
                    Arrays.asList(String.join(":", String.join(".", TEST_DOMAIN, modelId), modelVersion)),
                    "Error: There is no permission to modify model service " + MXE_MODEL_SERVICE_NAME);

            // try to modify model service in first domain with model from second domain, with second user who has read
            // rights in first domain
            serviceModifyModelFailedInStep(mxeCliDriver,
                    "Failed to modify model service in first domain with model from second domain, with second user who has read rights in first domain",
                    MXE_MODEL_SERVICE_NAME,
                    Arrays.asList(
                            String.join(":", String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion)),
                    "Error: There is no permission to modify model service " + MXE_MODEL_SERVICE_NAME);

            // try to modify model service in second domain with model from first domain, with second user who has read
            // rights in first domain
            serviceModifyModelFailedInStep(mxeCliDriver,
                    "Failed to modify model service in second domain with model from first domain, with second user who has no permissions in first domain",
                    MXE_MODEL_SERVICE_NAME + "-second",
                    Arrays.asList(String.join(":", String.join(".", TEST_DOMAIN, modelId), modelVersion)),
                    "Error: Model with ID \"" + String.join(".", TEST_DOMAIN, modelId) + "\" and version \""
                            + modelVersion + "\" does not exist, or there is no permission to use it");

            // Add all permission for the first dommain to second user
            if (testWithGroup) {
                addRoleToGroup(TEST_ROLE_ALL, TEST_GROUP_SECOND, true, false);
            } else {
                addRoleToUser(TEST_ROLE_ALL, TEST_USER_SECOND, true, false);
            }

            // Login with second user
            reloginWithUser(TEST_USER_SECOND, TEST_USER_PASSWORD);

            // Create service in first domain with second user who has full rights in first domain
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third", String.join(".", TEST_DOMAIN, modelId),
                    modelVersion, TEST_DOMAIN, 1);
            serviceListInStep(mxeCliDriver);
            verifyServiceDomain(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third", TEST_DOMAIN);
            // Delete service and model from first domain with second user, who has full access there
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third");

            // Try to create service in own second domain with model from first domain, with second user who has read
            // rights in first domain
            serviceCreateInStep(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third", String.join(".", TEST_DOMAIN, modelId),
                    modelVersion, TEST_DOMAIN_SECOND, 1);
            serviceListInStep(mxeCliDriver);
            verifyServiceDomain(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third", TEST_DOMAIN_SECOND);
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-third");

            // try to modify model service in second domain with model from first domain, with second user who has read
            // rights in first domain
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-second", String.join(".", TEST_DOMAIN, modelId),
                    modelVersion, true);

            // try to modify model service in second domain with model from second domain, with second user who has read
            // rights in first domain
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-second",
                    String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion, true);

            // try to modify model service in first domain with model from second domain, with second user who has full
            // rights in first domain
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME,
                    String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion, true);

            // try to modify model service in first domain with model from first domain, with second user who has full
            // rights in first domain
            serviceModifyModel(mxeCliDriver, MXE_MODEL_SERVICE_NAME, String.join(".", TEST_DOMAIN, modelId),
                    modelVersion, true);

            // Delete service and model from second domain with second user, who has full access there
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME + "-second");
            modelDelete(mxeCliDriver, String.join(".", TEST_DOMAIN_SECOND, secondModelId), secondModelVersion);

            // Login with first user
            reloginWithUser(TEST_USER, TEST_USER_PASSWORD);

            // Delete service and model from first domain
            serviceDelete(mxeCliDriver, MXE_MODEL_SERVICE_NAME);
            modelDelete(mxeCliDriver, String.join(".", TEST_DOMAIN, modelId), modelVersion);

            // Delete users and roles
            if (testWithGroup) {
                revokeRoleFromGroupAndDelete(TEST_ROLE_READ, TEST_GROUP_SECOND, true, false);
                deleteAccessControlInStep(TEST_USER_SECOND, TEST_GROUP_SECOND, TEST_SECOND_ROLE_ALL);
                deleteAccessControlInStep(TEST_USER, TEST_GROUP, TEST_ROLE_ALL);
            } else {
                revokeRoleFromUserAndDelete(TEST_ROLE_READ, TEST_USER_SECOND, true);
                deleteAccessControlInStep(TEST_USER_SECOND, TEST_SECOND_ROLE_ALL);
                deleteAccessControlInStep(TEST_USER, TEST_ROLE_ALL);
            }

            // Login with original
            reloginWithUser(mxeUser.getUserName(), mxeUser.getPassword());

        } catch (Exception e) {
            setTestError(ERROR_RESOURCE_RELEASE, e);
        }
    }

    private void reloginWithUser(String userName, String userPassword) throws IOException {
        setTestStepBegin("Relogin with " + userName);
        saveAssertTrue("Failed to login to MXE with " + userName,
                AccessControlUtil.relogin(mxeCluster, cluster, userName, userPassword));
    }

    private void setupAccessControlInStep(String userName, String userPassword, String roleForUserName,
            String roleDomain, RolePermission rolePermission, RoleType roleType) {
        setTestStepBegin("Setting up access control, user " + userName + " with role " + roleForUserName + " "
                + roleDomain + ":" + roleType + ":" + rolePermission);
        createUser(userName, userPassword, false, true);
        createRoleAndAddToUser(userName, roleForUserName, roleDomain, rolePermission, roleType, false);
    }

    private void createUser(String userName, String userPassword, boolean inStep, boolean inSubStep) {
        setStep("Create user " + userName, inStep, inSubStep);
        saveAssertTrue("Failed to create user " + userName,
                keycloakDriver.createUserWithRole(userName, userPassword, MXE_MODEL_SERVING_ROLE).isPresent());
    }


    private void createRoleAndAddToUser(String userName, String roleForUserName, String roleDomain,
            RolePermission rolePermission, RoleType roleType, boolean inStep) {
        setStep("Create role " + roleForUserName + " " + roleDomain + ":" + roleType + ":" + rolePermission
                + " and add it to user " + userName, inStep, false);
        createRole(roleForUserName, roleDomain, rolePermission, roleType, false, true);
        addRoleToUser(roleForUserName, userName, false, true);
    }

    private void createRole(String roleName, String roleDomain, RolePermission rolePermission, RoleType roleType,
            boolean inStep, boolean inSubStep) {
        setStep("Create role " + roleName + " " + roleDomain + ":" + roleType + ":" + rolePermission, inStep,
                inSubStep);
        saveAssertTrue("Failed to create role " + roleName,
                keycloakDriver.createRole(roleName, roleDomain, rolePermission, roleType));
    }

    private void addRoleToUser(String roleForUserName, String userName, boolean inStep, boolean inSubStep) {
        setStep("Add role " + roleForUserName + " to user " + userName, inStep, inSubStep);
        saveAssertTrue("Failed to add role " + roleForUserName + " to user " + userName,
                keycloakDriver.addRoleToUser(roleForUserName, userName));
    }

    private void setupAccessControlInStep(String userName, String userPassword, String groupName,
            String roleForGroupName, String roleDomain, RolePermission rolePermission, RoleType roleType) {
        setTestStepBegin("Setting up access control, user " + userName + " with group " + groupName
                + ", group with role " + roleForGroupName + " " + roleDomain + ":" + roleType + ":" + rolePermission);
        createUser(userName, userPassword, false, true);
        createGroupAndAddToUser(groupName, userName, false);
        createRoleAndAddToGroup(groupName, roleForGroupName, roleDomain, rolePermission, roleType, false);
    }

    private void createGroup(String groupName, boolean inStep, boolean inSubStep) {
        setStep("Create goup " + groupName, inStep, inSubStep);
        saveAssertTrue("Failed to create group " + groupName, keycloakDriver.createGroup(groupName).isPresent());
    }

    private void addRoleToGroup(String roleName, String groupName, boolean inStep, boolean inSubStep) {
        setStep("Add role " + roleName + " to group " + groupName, inStep, inSubStep);
        saveAssertTrue("Failed to add role " + roleName + " to group " + groupName,
                keycloakDriver.addRoleToGroup(roleName, groupName));
    }

    private void addGroupToUser(String groupName, String userName, boolean inStep, boolean inSubStep) {
        setStep("Add group " + groupName + " to user " + userName, inStep, inSubStep);
        saveAssertTrue("Failed to add group " + groupName + " to user " + userName,
                keycloakDriver.addGroupToUser(groupName, userName));
    }

    private void createGroupAndAddToUser(String groupName, String userName, boolean inStep) {
        setStep("Create group " + groupName + " and add to user " + userName, inStep, false);
        createGroup(groupName, false, true);
        addGroupToUser(groupName, userName, false, true);
    }

    private void createRoleAndAddToGroup(String groupName, String roleForGroupName, String roleDomain,
            RolePermission rolePermission, RoleType roleType, boolean inStep) {
        setStep("Create role " + roleForGroupName + " " + roleDomain + ":" + roleType + ":" + rolePermission
                + " and add to group " + groupName, inStep, false);
        createRole(roleForGroupName, roleDomain, rolePermission, roleType, false, true);
        addRoleToGroup(roleForGroupName, groupName, false, true);
    }

    private void setStep(String stepName, boolean inStep, boolean inSubStep) {
        if (inStep) {
            setTestStepBegin(stepName);
        } else if (inSubStep) {
            setSubTestStep(stepName);
        }
    }

    private void deleteAccessControlInStep(String userName, String roleName) {
        setTestStepBegin("Delete access control settings, user with role");
        setSubTestStep("Revoke role from user delete it");
        revokeRoleFromUserAndDelete(roleName, userName, false);

        setSubTestStep("Delete user " + userName);
        saveAssertTrue("Failed to delete user " + userName, keycloakDriver.deleteUser(userName));
    }

    private void revokeRoleFromUserAndDelete(String roleName, String userName, boolean inStep) {
        setStep("Revoke role " + roleName + " from user " + userName + " and delete it", inStep, false);
        saveAssertTrue("Failed to revoke role " + roleName + " from user " + userName,
                keycloakDriver.revokeRoleFromUser(roleName, userName));

        saveAssertTrue("Failed to delete role " + roleName, keycloakDriver.deleteRole(roleName));
    }

    private void deleteAccessControlInStep(String userName, String groupName, String roleName) {
        setTestStepBegin("Delete access control settings, user with group");

        revokeRoleFromGroupAndDelete(roleName, groupName, false, true);

        setSubTestStep("Revoke group " + groupName + " from user " + userName + " and delete it");
        saveAssertTrue("Failed to revoke group " + groupName + " from user " + userName,
                keycloakDriver.revokeGroupFromUser(groupName, userName));
        saveAssertTrue("Failed to delete group " + groupName, keycloakDriver.deleteGroup(groupName));

        setSubTestStep("Delete user " + userName);
        saveAssertTrue("Failed to delete user " + userName, keycloakDriver.deleteUser(userName));
    }

    private void revokeRoleFromGroupAndDelete(String roleName, String groupName, boolean inStep, boolean inSubStep) {
        setStep("Revoke role " + roleName + " from group " + groupName + " and delete it", inStep, inSubStep);
        saveAssertTrue("Failed to revoke role " + roleName + " from group " + groupName,
                keycloakDriver.revokeRoleFromGroup(roleName, groupName));
        saveAssertTrue("Failed to delete role " + roleName, keycloakDriver.deleteRole(roleName));
    }
}
