/**
 * The MIT License
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.restapi.openapi;

import ee.ria.xroad.common.identifier.ClientId;
import ee.ria.xroad.common.identifier.GlobalGroupId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.niis.xroad.restapi.facade.GlobalConfFacade;
import org.niis.xroad.restapi.openapi.model.Service;
import org.niis.xroad.restapi.openapi.model.ServiceClient;
import org.niis.xroad.restapi.openapi.model.ServiceUpdate;
import org.niis.xroad.restapi.openapi.model.Subject;
import org.niis.xroad.restapi.openapi.model.SubjectType;
import org.niis.xroad.restapi.openapi.model.Subjects;
import org.niis.xroad.restapi.service.GlobalConfService;
import org.niis.xroad.restapi.util.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.niis.xroad.restapi.service.AccessRightService.AccessRightNotFoundException.ERROR_ACCESSRIGHT_NOT_FOUND;
import static org.niis.xroad.restapi.service.ClientNotFoundException.ERROR_CLIENT_NOT_FOUND;
import static org.niis.xroad.restapi.service.ServiceNotFoundException.ERROR_SERVICE_NOT_FOUND;

/**
 * Test ServicesApiController
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class ServicesApiControllerIntegrationTest {

    private static final String SS1_PREDICT_WINNING_LOTTERY_NUMBERS = "FI:GOV:M1:SS1:predictWinningLotteryNumbers.v1";
    private static final String FOO = "foo";

    @Autowired
    private ServicesApiController servicesApiController;

    @MockBean
    private GlobalConfFacade globalConfFacade;

    @MockBean
    private GlobalConfService globalConfService;

    @Before
    public void setup() {
        when(globalConfFacade.getGlobalGroupDescription(any())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            GlobalGroupId id = (GlobalGroupId) args[0];
            return TestUtils.NAME_FOR + id.getGroupCode();
        });

        when(globalConfFacade.getMemberName(any())).thenAnswer((Answer<String>) invocation -> {
            Object[] args = invocation.getArguments();
            ClientId identifier = (ClientId) args[0];
            return TestUtils.NAME_FOR + identifier.toShortString().replace("/", ":");
        });
        when(globalConfFacade.getMembers(any())).thenReturn(new ArrayList<>(Arrays.asList(
                TestUtils.getMemberInfo(TestUtils.INSTANCE_FI, TestUtils.MEMBER_CLASS_GOV, TestUtils.MEMBER_CODE_M1,
                        null),
                TestUtils.getMemberInfo(TestUtils.INSTANCE_FI, TestUtils.MEMBER_CLASS_GOV, TestUtils.MEMBER_CODE_M1,
                        TestUtils.SUBSYSTEM1),
                TestUtils.getMemberInfo(TestUtils.INSTANCE_FI, TestUtils.MEMBER_CLASS_GOV, TestUtils.MEMBER_CODE_M1,
                        TestUtils.SUBSYSTEM2),
                TestUtils.getMemberInfo(TestUtils.INSTANCE_EE, TestUtils.MEMBER_CLASS_GOV, TestUtils.MEMBER_CODE_M2,
                        TestUtils.SUBSYSTEM3),
                TestUtils.getMemberInfo(TestUtils.INSTANCE_EE, TestUtils.MEMBER_CLASS_GOV, TestUtils.MEMBER_CODE_M1,
                        null),
                TestUtils.getMemberInfo(TestUtils.INSTANCE_EE, TestUtils.MEMBER_CLASS_PRO, TestUtils.MEMBER_CODE_M1,
                        TestUtils.SUBSYSTEM1),
                TestUtils.getMemberInfo(TestUtils.INSTANCE_EE, TestUtils.MEMBER_CLASS_PRO, TestUtils.MEMBER_CODE_M2,
                        null))
        ));
    }

    @Test
    @WithMockUser(authorities = { "VIEW_CLIENT_SERVICES", "EDIT_SERVICE_PARAMS" })
    public void updateServiceHttps() {
        Service service = servicesApiController.getService(TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(60, service.getTimeout().intValue());

        service.setTimeout(10);
        service.setSslAuth(false);
        service.setUrl(TestUtils.URL_HTTPS);
        ServiceUpdate serviceUpdate = new ServiceUpdate().service(service);

        Service updatedService = servicesApiController.updateService(TestUtils.SS1_GET_RANDOM_V1,
                serviceUpdate).getBody();
        assertEquals(10, updatedService.getTimeout().intValue());
        assertEquals(false, updatedService.getSslAuth());
        assertEquals(TestUtils.URL_HTTPS, updatedService.getUrl());
    }

    @Test
    @WithMockUser(authorities = { "VIEW_CLIENT_SERVICES", "EDIT_SERVICE_PARAMS" })
    public void updateServiceHttp() {
        Service service = servicesApiController.getService(TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(60, service.getTimeout().intValue());

        service.setTimeout(10);
        service.setSslAuth(true); // value does not matter if http - will aways be set to null
        service.setUrl(TestUtils.URL_HTTP);
        ServiceUpdate serviceUpdate = new ServiceUpdate().service(service);

        Service updatedService = servicesApiController.updateService(TestUtils.SS1_GET_RANDOM_V1,
                serviceUpdate).getBody();
        assertEquals(10, updatedService.getTimeout().intValue());
        assertNull(updatedService.getSslAuth());
        assertEquals(TestUtils.URL_HTTP, updatedService.getUrl());
    }

    @Test
    @WithMockUser(authorities = { "VIEW_CLIENT_SERVICES", "EDIT_SERVICE_PARAMS" })
    public void updateServiceAll() {
        Service service = servicesApiController.getService(TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(60, service.getTimeout().intValue());

        service.setTimeout(10);
        service.setSslAuth(false);
        service.setUrl(TestUtils.URL_HTTPS);
        ServiceUpdate serviceUpdate = new ServiceUpdate().service(service).urlAll(true)
                .sslAuthAll(true).timeoutAll(true);

        Service updatedService = servicesApiController.updateService(TestUtils.SS1_GET_RANDOM_V1,
                serviceUpdate).getBody();
        assertEquals(10, updatedService.getTimeout().intValue());
        assertEquals(false, updatedService.getSslAuth());
        assertEquals(TestUtils.URL_HTTPS, updatedService.getUrl());

        Service otherServiceFromSameServiceDesc = servicesApiController.getService(
                TestUtils.SS1_CALCULATE_PRIME).getBody();

        assertEquals(10, otherServiceFromSameServiceDesc.getTimeout().intValue());
        assertEquals(false, otherServiceFromSameServiceDesc.getSslAuth());
        assertEquals(TestUtils.URL_HTTPS, otherServiceFromSameServiceDesc.getUrl());
    }

    @Test
    @WithMockUser(authorities = { "VIEW_CLIENT_SERVICES", "EDIT_SERVICE_PARAMS" })
    public void updateServiceOnlyUrlAll() {
        Service service = servicesApiController.getService(TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(60, service.getTimeout().intValue());

        service.setTimeout(10);
        service.setSslAuth(true);
        service.setUrl(TestUtils.URL_HTTPS);
        ServiceUpdate serviceUpdate = new ServiceUpdate().service(service).urlAll(true);

        Service updatedService = servicesApiController.updateService(TestUtils.SS1_GET_RANDOM_V1,
                serviceUpdate).getBody();
        assertEquals(10, updatedService.getTimeout().intValue());
        assertEquals(true, updatedService.getSslAuth());
        assertEquals(TestUtils.URL_HTTPS, updatedService.getUrl());

        Service otherServiceFromSameServiceDesc = servicesApiController.getService(
                TestUtils.SS1_CALCULATE_PRIME).getBody();

        assertEquals(60, otherServiceFromSameServiceDesc.getTimeout().intValue());
        assertEquals(false, otherServiceFromSameServiceDesc.getSslAuth());
        assertEquals(TestUtils.URL_HTTPS, otherServiceFromSameServiceDesc.getUrl());
    }

    @Test
    @WithMockUser(authorities = { "VIEW_CLIENT_SERVICES" })
    public void getServiceClientNotFound() {
        try {
            servicesApiController.getService(TestUtils.SS0_GET_RANDOM_V1).getBody();
            fail("should throw ResourceNotFoundException");
        } catch (ResourceNotFoundException expected) {
            assertEquals(ERROR_CLIENT_NOT_FOUND, expected.getErrorDeviation().getCode());
        }
    }

    @Test
    @WithMockUser(authorities = { "VIEW_CLIENT_SERVICES" })
    public void getServiceNotFound() {
        try {
            servicesApiController.getService(SS1_PREDICT_WINNING_LOTTERY_NUMBERS).getBody();
            fail("should throw ResourceNotFoundException");
        } catch (ResourceNotFoundException expected) {
            assertEquals(ERROR_SERVICE_NOT_FOUND, expected.getErrorDeviation().getCode());
        }
    }

    @Test
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL" })
    public void getServiceAccessRights() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        ServiceClient serviceClient = getServiceClientByType(serviceClients, TestUtils.GLOBALGROUP).get();
        assertEquals(TestUtils.NAME_FOR + TestUtils.DB_GLOBALGROUP_CODE,
                serviceClient.getSubject().getMemberNameGroupDescription());
        assertEquals(TestUtils.DB_GLOBALGROUP_ID, serviceClient.getSubject().getId());
        assertEquals(TestUtils.GLOBALGROUP, serviceClient.getSubject().getSubjectType().name());
        assertNull(serviceClient.getAccessRights());

        serviceClient = getServiceClientByType(serviceClients, TestUtils.LOCALGROUP).get();
        assertEquals(TestUtils.DB_LOCAL_GROUP_ID_1, serviceClient.getSubject().getId());
        assertEquals(TestUtils.DB_LOCAL_GROUP_CODE, serviceClient.getSubject().getLocalGroupCode());
        assertEquals(FOO, serviceClient.getSubject().getMemberNameGroupDescription());
        assertEquals(TestUtils.LOCALGROUP, serviceClient.getSubject().getSubjectType().name());
        assertNull(serviceClient.getAccessRights());

        serviceClient = getServiceClientByType(serviceClients, TestUtils.SUBSYSTEM).get();
        assertEquals(TestUtils.NAME_FOR + TestUtils.CLIENT_ID_SS2,
                serviceClient.getSubject().getMemberNameGroupDescription());
        assertEquals(TestUtils.CLIENT_ID_SS2, serviceClient.getSubject().getId());
        assertEquals(TestUtils.SUBSYSTEM, serviceClient.getSubject().getSubjectType().name());
        assertNull(serviceClient.getAccessRights());

        serviceClients = servicesApiController.getServiceAccessRights(TestUtils.SS1_CALCULATE_PRIME).getBody();
        assertTrue(serviceClients.isEmpty());

        // different versions of a service should have the same access rights
        serviceClients = servicesApiController.getServiceAccessRights(TestUtils.SS1_GET_RANDOM_V2).getBody();
        assertEquals(3, serviceClients.size());

        try {
            servicesApiController.getServiceAccessRights(TestUtils.SS0_GET_RANDOM_V1);
            fail("should throw ResourceNotFoundException");
        } catch (ResourceNotFoundException expected) {
            assertEquals(ERROR_CLIENT_NOT_FOUND, expected.getErrorDeviation().getCode());
        }

        try {
            servicesApiController.getServiceAccessRights(SS1_PREDICT_WINNING_LOTTERY_NUMBERS);
            fail("should throw ResourceNotFoundException");
        } catch (ResourceNotFoundException expected) {
            assertEquals(ERROR_SERVICE_NOT_FOUND, expected.getErrorDeviation().getCode());
        }
    }

    private Optional<ServiceClient> getServiceClientByType(List<ServiceClient> serviceClients, String type) {
        return serviceClients
                .stream()
                .filter(serviceClient -> serviceClient.getSubject().getSubjectType().name().equals(type))
                .findFirst();
    }

    @Test
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteServiceAccessRights() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_GLOBALGROUP_ID).subjectType(SubjectType.GLOBALGROUP));

        servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
        serviceClients = servicesApiController.getServiceAccessRights(TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(2, serviceClients.size());
    }

    @Test
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteMultipleServiceAccessRights() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_GLOBALGROUP_ID).subjectType(SubjectType.GLOBALGROUP))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS2).subjectType(SubjectType.SUBSYSTEM));

        servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
        serviceClients = servicesApiController.getServiceAccessRights(TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(1, serviceClients.size());
    }

    @Test
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteMultipleSameServiceAccessRights() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_GLOBALGROUP_ID).subjectType(SubjectType.GLOBALGROUP))
                .addItemsItem(new Subject().id(TestUtils.DB_GLOBALGROUP_ID).subjectType(SubjectType.GLOBALGROUP));

        servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
        serviceClients = servicesApiController.getServiceAccessRights(TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(2, serviceClients.size());
    }

    @Test(expected = BadRequestException.class)
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteServiceAccessRightsWrongType() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS2).subjectType(SubjectType.GLOBALGROUP));
        servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
    }

    @Test(expected = BadRequestException.class)
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteServiceAccessRightsWrongTypeLocalGroup() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS2).subjectType(SubjectType.LOCALGROUP));
        servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
    }

    @Test
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteServiceAccessRightsWithRedundantSubjects() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS2).subjectType(SubjectType.SUBSYSTEM))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS3).subjectType(SubjectType.SUBSYSTEM))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS4).subjectType(SubjectType.SUBSYSTEM));
        try {
            servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
        } catch (BadRequestException expected) {
            assertEquals(ERROR_ACCESSRIGHT_NOT_FOUND, expected.getErrorDeviation().getCode());
        }
    }

    @Test
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteServiceAccessRightsLocalGroupsWithRedundantSubjects() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_LOCAL_GROUP_ID_1).subjectType(SubjectType.LOCALGROUP))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS3).subjectType(SubjectType.SUBSYSTEM))
                .addItemsItem(new Subject().id(TestUtils.DB_LOCAL_GROUP_ID_2).subjectType(SubjectType.LOCALGROUP));
        try {
            servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
        } catch (BadRequestException expected) {
            assertEquals(ERROR_ACCESSRIGHT_NOT_FOUND, expected.getErrorDeviation().getCode());
        }
    }

    @Test(expected = BadRequestException.class)
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteServiceAccessRightsWrongLocalGroupId() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_LOCAL_GROUP_CODE).subjectType(SubjectType.LOCALGROUP))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS3).subjectType(SubjectType.SUBSYSTEM))
                .addItemsItem(new Subject().id(TestUtils.DB_LOCAL_GROUP_ID_2).subjectType(SubjectType.LOCALGROUP));
        servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
    }

    @Test(expected = BadRequestException.class)
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void deleteServiceAccessRightsWrongLocalGroupType() {
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjects = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_LOCAL_GROUP_ID_2).subjectType(SubjectType.GLOBALGROUP))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS3).subjectType(SubjectType.SUBSYSTEM));
        servicesApiController.deleteServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjects).getBody();
    }

    @Test
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void addAccessRights() {
        when(globalConfService.clientIdentifiersExist(any())).thenReturn(true);
        when(globalConfService.globalGroupIdentifiersExist(any())).thenReturn(true);
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_CALCULATE_PRIME).getBody();
        assertEquals(0, serviceClients.size());

        Subjects subjectsToAdd = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_LOCAL_GROUP_ID_2).subjectType(SubjectType.LOCALGROUP))
                .addItemsItem(new Subject().id(TestUtils.DB_GLOBALGROUP_ID).subjectType(SubjectType.GLOBALGROUP))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS2).subjectType(SubjectType.SUBSYSTEM));

        List<ServiceClient> updatedServiceClients = servicesApiController
                .addServiceAccessRight(TestUtils.SS1_CALCULATE_PRIME, subjectsToAdd).getBody();

        assertEquals(3, updatedServiceClients.size());
    }

    @Test(expected = ConflictException.class)
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void addDuplicateAccessRight() {
        when(globalConfService.clientIdentifiersExist(any())).thenReturn(true);
        when(globalConfService.globalGroupIdentifiersExist(any())).thenReturn(true);
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjectsToAdd = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_LOCAL_GROUP_ID_2).subjectType(SubjectType.LOCALGROUP))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS2).subjectType(SubjectType.SUBSYSTEM));

        servicesApiController.addServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjectsToAdd);
    }

    @Test(expected = BadRequestException.class)
    @WithMockUser(authorities = { "VIEW_SERVICE_ACL", "EDIT_SERVICE_ACL" })
    public void addBogusAccessRight() {
        when(globalConfService.clientIdentifiersExist(any())).thenReturn(false);
        when(globalConfService.globalGroupIdentifiersExist(any())).thenReturn(false);
        List<ServiceClient> serviceClients = servicesApiController.getServiceAccessRights(
                TestUtils.SS1_GET_RANDOM_V1).getBody();
        assertEquals(3, serviceClients.size());

        Subjects subjectsToAdd = new Subjects()
                .addItemsItem(new Subject().id(TestUtils.DB_LOCAL_GROUP_ID_2).subjectType(SubjectType.LOCALGROUP))
                .addItemsItem(new Subject().id(TestUtils.CLIENT_ID_SS2 + "foo").subjectType(SubjectType.SUBSYSTEM));

        servicesApiController.addServiceAccessRight(TestUtils.SS1_GET_RANDOM_V1, subjectsToAdd);
    }
}
