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

import ee.ria.xroad.common.conf.serverconf.model.CertificateType;
import ee.ria.xroad.common.conf.serverconf.model.ClientType;
import ee.ria.xroad.common.conf.serverconf.model.LocalGroupType;
import ee.ria.xroad.common.conf.serverconf.model.ServiceDescriptionType;
import ee.ria.xroad.common.identifier.ClientId;
import ee.ria.xroad.common.identifier.XRoadObjectType;
import ee.ria.xroad.signer.protocol.dto.CertificateInfo;

import lombok.extern.slf4j.Slf4j;
import org.niis.xroad.restapi.converter.CertificateDetailsConverter;
import org.niis.xroad.restapi.converter.ClientConverter;
import org.niis.xroad.restapi.converter.ConnectionTypeMapping;
import org.niis.xroad.restapi.converter.EndpointHelper;
import org.niis.xroad.restapi.converter.LocalGroupConverter;
import org.niis.xroad.restapi.converter.ServiceDescriptionConverter;
import org.niis.xroad.restapi.converter.SubjectConverter;
import org.niis.xroad.restapi.converter.SubjectTypeMapping;
import org.niis.xroad.restapi.converter.TokenCertificateConverter;
import org.niis.xroad.restapi.dto.AccessRightHolderDto;
import org.niis.xroad.restapi.exceptions.ErrorDeviation;
import org.niis.xroad.restapi.openapi.model.CertificateDetails;
import org.niis.xroad.restapi.openapi.model.Client;
import org.niis.xroad.restapi.openapi.model.ConnectionType;
import org.niis.xroad.restapi.openapi.model.ConnectionTypeWrapper;
import org.niis.xroad.restapi.openapi.model.LocalGroup;
import org.niis.xroad.restapi.openapi.model.ServiceDescription;
import org.niis.xroad.restapi.openapi.model.ServiceDescriptionAdd;
import org.niis.xroad.restapi.openapi.model.ServiceType;
import org.niis.xroad.restapi.openapi.model.Subject;
import org.niis.xroad.restapi.openapi.model.SubjectType;
import org.niis.xroad.restapi.openapi.model.TokenCertificate;
import org.niis.xroad.restapi.service.AccessRightService;
import org.niis.xroad.restapi.service.CertificateAlreadyExistsException;
import org.niis.xroad.restapi.service.CertificateNotFoundException;
import org.niis.xroad.restapi.service.ClientNotFoundException;
import org.niis.xroad.restapi.service.ClientService;
import org.niis.xroad.restapi.service.InvalidUrlException;
import org.niis.xroad.restapi.service.LocalGroupService;
import org.niis.xroad.restapi.service.MissingParameterException;
import org.niis.xroad.restapi.service.ServiceDescriptionService;
import org.niis.xroad.restapi.service.TokenService;
import org.niis.xroad.restapi.service.UnhandledWarningsException;
import org.niis.xroad.restapi.util.ResourceUtils;
import org.niis.xroad.restapi.wsdl.InvalidWsdlException;
import org.niis.xroad.restapi.wsdl.OpenApiParser;
import org.niis.xroad.restapi.wsdl.WsdlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.niis.xroad.restapi.openapi.ApiUtil.createCreatedResponse;

/**
 * clients api
 */
@Controller
@RequestMapping("/api")
@Slf4j
@PreAuthorize("denyAll")
public class ClientsApiController implements ClientsApi {
    public static final String ERROR_INVALID_CERT = "invalid_cert";

    private final ClientConverter clientConverter;
    private final ClientService clientService;
    private final LocalGroupConverter localGroupConverter;
    private final LocalGroupService localGroupService;
    private final TokenService tokenService;
    private final CertificateDetailsConverter certificateDetailsConverter;
    private final ServiceDescriptionConverter serviceDescriptionConverter;
    private final ServiceDescriptionService serviceDescriptionService;
    private final AccessRightService accessRightService;
    private final SubjectConverter subjectConverter;
    private final TokenCertificateConverter tokenCertificateConverter;
    private final EndpointHelper endpointService;

    /**
     * ClientsApiController constructor
     *
     * @param clientService
     * @param tokenService
     * @param clientConverter
     * @param localGroupConverter
     * @param localGroupService
     * @param serviceDescriptionConverter
     * @param serviceDescriptionService
     * @param accessRightService
     * @param subjectConverter
     * @param tokenCertificateConverter
     */

    @Autowired
    public ClientsApiController(ClientService clientService, TokenService tokenService,
            ClientConverter clientConverter, LocalGroupConverter localGroupConverter,
            LocalGroupService localGroupService, CertificateDetailsConverter certificateDetailsConverter,
            ServiceDescriptionConverter serviceDescriptionConverter,
            ServiceDescriptionService serviceDescriptionService, AccessRightService accessRightService,
            SubjectConverter subjectConverter, TokenCertificateConverter tokenCertificateConverter,
            EndpointHelper endpointService) {
        this.clientService = clientService;
        this.tokenService = tokenService;
        this.clientConverter = clientConverter;
        this.localGroupConverter = localGroupConverter;
        this.localGroupService = localGroupService;
        this.certificateDetailsConverter = certificateDetailsConverter;
        this.serviceDescriptionConverter = serviceDescriptionConverter;
        this.serviceDescriptionService = serviceDescriptionService;
        this.accessRightService = accessRightService;
        this.subjectConverter = subjectConverter;
        this.tokenCertificateConverter = tokenCertificateConverter;
        this.endpointService = endpointService;
    }

    /**
     * Finds clients matching search terms
     *
     * @param name
     * @param instance
     * @param memberClass
     * @param memberCode
     * @param subsystemCode
     * @param showMembers include members (without susbsystemCode) in the results
     * @param internalSearch search only in the local clients
     * @return
     */
    @Override
    @PreAuthorize("hasAuthority('VIEW_CLIENTS')")
    public ResponseEntity<List<Client>> findClients(String name, String instance, String memberClass,
            String memberCode, String subsystemCode, Boolean showMembers, Boolean internalSearch) {
        boolean unboxedShowMembers = Boolean.TRUE.equals(showMembers);
        boolean unboxedInternalSearch = Boolean.TRUE.equals(internalSearch);
        List<Client> clients = clientConverter.convert(clientService.findClients(name,
                instance, memberClass, memberCode, subsystemCode, unboxedShowMembers, unboxedInternalSearch));
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CLIENT_DETAILS')")
    public ResponseEntity<Client> getClient(String id) {
        ClientType clientType = getClientType(id);
        Client client = clientConverter.convert(clientType);
        return new ResponseEntity<>(client, HttpStatus.OK);
    }

    /**
     * Read one client from DB
     *
     * @param encodedId id that is encoded with the <INSTANCE>:<MEMBER_CLASS>:....
     * encoding
     * @return
     * @throws ResourceNotFoundException if client does not exist
     * @throws BadRequestException if encodedId was not proper encoded client ID
     */
    private ClientType getClientType(String encodedId) {
        ClientId clientId = clientConverter.convertId(encodedId);
        ClientType clientType = clientService.getClient(clientId);
        if (clientType == null) {
            throw new ResourceNotFoundException("client with id " + encodedId + " not found");
        }
        return clientType;
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CLIENT_DETAILS')")
    public ResponseEntity<List<TokenCertificate>> getClientSignCertificates(String encodedId) {
        ClientType clientType = getClientType(encodedId);
        List<CertificateInfo> certificateInfos = tokenService.getSignCertificates(clientType);
        List<TokenCertificate> certificates = tokenCertificateConverter.convert(certificateInfos);
        return new ResponseEntity<>(certificates, HttpStatus.OK);
    }

    /**
     * Update a client's connection type
     *
     * @param encodedId
     * @param connectionTypeWrapper wrapper object containing the connection type to set
     * @return
     */
    @PreAuthorize("hasAuthority('EDIT_CLIENT_INTERNAL_CONNECTION_TYPE')")
    @Override
    public ResponseEntity<Client> updateClient(String encodedId, ConnectionTypeWrapper connectionTypeWrapper) {
        if (connectionTypeWrapper == null || connectionTypeWrapper.getConnectionType() == null) {
            throw new BadRequestException();
        }
        ConnectionType connectionType = connectionTypeWrapper.getConnectionType();
        ClientId clientId = clientConverter.convertId(encodedId);
        String connectionTypeString = ConnectionTypeMapping.map(connectionType).get();
        ClientType changed = null;
        try {
            changed = clientService.updateConnectionType(clientId, connectionTypeString);
        } catch (ClientNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }
        Client result = clientConverter.convert(changed);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADD_CLIENT_INTERNAL_CERT')")
    public ResponseEntity<CertificateDetails> addClientTlsCertificate(String encodedId,
            Resource body) {
        byte[] certificateBytes = ResourceUtils.springResourceToBytesOrThrowBadRequest(body);
        ClientId clientId = clientConverter.convertId(encodedId);
        CertificateType certificateType = null;
        try {
            certificateType = clientService.addTlsCertificate(clientId, certificateBytes);
        } catch (CertificateException c) {
            throw new BadRequestException(c, new ErrorDeviation(ERROR_INVALID_CERT));
        } catch (ClientNotFoundException e) {
            throw new ResourceNotFoundException(e);
        } catch (CertificateAlreadyExistsException e) {
            throw new ConflictException(e);
        }
        CertificateDetails certificateDetails = certificateDetailsConverter.convert(certificateType);
        return createCreatedResponse("/api/clients/{id}/tls-certificates/{hash}", certificateDetails, encodedId,
                certificateDetails.getHash());
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_CLIENT_INTERNAL_CERT')")
    public ResponseEntity<Void> deleteClientTlsCertificate(String encodedId, String hash) {
        ClientId clientId = clientConverter.convertId(encodedId);
        try {
            clientService.deleteTlsCertificate(clientId, hash);
        } catch (ClientNotFoundException | CertificateNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CLIENT_INTERNAL_CERT_DETAILS')")
    public ResponseEntity<CertificateDetails> getClientTlsCertificate(String encodedId, String certHash) {
        ClientId clientId = clientConverter.convertId(encodedId);
        Optional<CertificateType> certificateType = null;
        try {
            certificateType = clientService.getTlsCertificate(clientId, certHash);
        } catch (ClientNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }
        if (!certificateType.isPresent()) {
            throw new ResourceNotFoundException("certificate with hash " + certHash
                    + ", client id " + encodedId + " not found");
        }
        return new ResponseEntity<>(certificateDetailsConverter.convert(certificateType.get()), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CLIENT_INTERNAL_CERTS')")
    public ResponseEntity<List<CertificateDetails>> getClientTlsCertificates(String encodedId) {
        ClientType clientType = getClientType(encodedId);
        List<CertificateDetails> certificates = clientService.getClientIsCerts(clientType.getIdentifier())
                .stream()
                .map(certificateDetailsConverter::convert)
                .collect(toList());
        return new ResponseEntity<>(certificates, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('ADD_LOCAL_GROUP')")
    public ResponseEntity<LocalGroup> addClientGroup(String id, LocalGroup localGroup) {
        ClientType clientType = getClientType(id);
        LocalGroupType localGroupType = null;
        try {
            localGroupType = localGroupService.addLocalGroup(clientType.getIdentifier(),
                    localGroupConverter.convert(localGroup));
        } catch (LocalGroupService.DuplicateLocalGroupCodeException e) {
            throw new ConflictException(e);
        } catch (ClientNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }
        LocalGroup createdGroup = localGroupConverter.convert(localGroupType);
        return createCreatedResponse("/api/local-groups/{id}", createdGroup, localGroupType.getId());
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CLIENT_LOCAL_GROUPS')")
    public ResponseEntity<List<LocalGroup>> getClientGroups(String encodedId) {
        ClientType clientType = getClientType(encodedId);
        List<LocalGroupType> localGroupTypes = clientService.getClientLocalGroups(clientType.getIdentifier());
        return new ResponseEntity<>(localGroupConverter.convert(localGroupTypes), HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CLIENT_SERVICES')")
    public ResponseEntity<List<ServiceDescription>> getClientServiceDescriptions(String encodedId) {
        ClientType clientType = getClientType(encodedId);
        List<ServiceDescription> serviceDescriptions = serviceDescriptionConverter.convert(
                clientService.getClientServiceDescriptions(clientType.getIdentifier()));

        return new ResponseEntity<>(serviceDescriptions, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ADD_WSDL', 'ADD_OPENAPI3')")
    public ResponseEntity<ServiceDescription> addClientServiceDescription(String id,
            ServiceDescriptionAdd serviceDescription) {
        ClientId clientId = clientConverter.convertId(id);
        String url = serviceDescription.getUrl();
        boolean ignoreWarnings = serviceDescription.getIgnoreWarnings();
        String restServiceCode = serviceDescription.getRestServiceCode();

        ServiceDescriptionType addedServiceDescriptionType = null;
        if (serviceDescription.getType() == ServiceType.WSDL) {
            try {
                addedServiceDescriptionType = serviceDescriptionService.addWsdlServiceDescription(
                        clientId, url, ignoreWarnings);
            } catch (WsdlParser.WsdlNotFoundException | UnhandledWarningsException
                    | InvalidUrlException | InvalidWsdlException e) {
                // deviation data (errorcode + warnings) copied
                throw new BadRequestException(e);
            } catch (ClientNotFoundException e) {
                // deviation data (errorcode + warnings) copied
                throw new ResourceNotFoundException(e);
            } catch (ServiceDescriptionService.ServiceAlreadyExistsException
                    | ServiceDescriptionService.WsdlUrlAlreadyExistsException e) {
                // deviation data (errorcode + warnings) copied
                throw new ConflictException(e);
            }
        } else if (serviceDescription.getType() == ServiceType.OPENAPI3) {
            try {
                addedServiceDescriptionType = serviceDescriptionService.addOpenapi3ServiceDescription(clientId, url,
                        restServiceCode, ignoreWarnings);
            } catch (OpenApiParser.ParsingException | UnhandledWarningsException | MissingParameterException e) {
                throw new BadRequestException(e);
            } catch (ClientNotFoundException e) {
                throw new ResourceNotFoundException(e);
            } catch (ServiceDescriptionService.UrlAlreadyExistsException
                    | ServiceDescriptionService.ServiceCodeAlreadyExistsException e) {
                throw new ConflictException(e);
            }
        } else if (serviceDescription.getType() == ServiceType.REST) {
            try {
                addedServiceDescriptionType = serviceDescriptionService.addRestEndpointServiceDescription(clientId,
                        url, restServiceCode);
            } catch (ClientNotFoundException e) {
                throw new ResourceNotFoundException(e);
            } catch (MissingParameterException e) {
                throw new BadRequestException(e);
            } catch (ServiceDescriptionService.ServiceCodeAlreadyExistsException
                    | ServiceDescriptionService.UrlAlreadyExistsException e) {
                throw new ConflictException(e);
            }
        }
        ServiceDescription addedServiceDescription = serviceDescriptionConverter.convert(
                addedServiceDescriptionType);
        return createCreatedResponse("/api/service-descriptions/{id}", addedServiceDescription,
                addedServiceDescription.getId());
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CLIENT_ACL_SUBJECTS')")
    public ResponseEntity<List<Subject>> findSubjects(String encodedClientId, String memberNameOrGroupDescription,
            SubjectType subjectType, String instance, String memberClass, String memberGroupCode,
            String subsystemCode) {
        ClientId clientId = clientConverter.convertId(encodedClientId);
        XRoadObjectType xRoadObjectType = SubjectTypeMapping.map(subjectType).orElse(null);
        List<AccessRightHolderDto> accessRightHolderDtos = null;
        try {
            accessRightHolderDtos = accessRightService.findAccessRightHolders(clientId, memberNameOrGroupDescription,
                    xRoadObjectType, instance, memberClass, memberGroupCode, subsystemCode);
        } catch (ClientNotFoundException e) {
            throw new ResourceNotFoundException(e);
        }
        List<Subject> subjects = subjectConverter.convert(accessRightHolderDtos);
        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }
}
