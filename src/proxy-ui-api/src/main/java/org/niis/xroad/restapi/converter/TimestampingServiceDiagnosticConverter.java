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
package org.niis.xroad.restapi.converter;

import ee.ria.xroad.common.DiagnosticsStatus;

import com.google.common.collect.Streams;
import org.niis.xroad.restapi.openapi.model.DiagnosticStatusClass;
import org.niis.xroad.restapi.openapi.model.TimestampingServiceDiagnostics;
import org.niis.xroad.restapi.openapi.model.TimestampingStatus;
import org.niis.xroad.restapi.util.FormatUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converter for timestamping service diagnostics related data between openapi and service domain classes
 */
@Component
public class TimestampingServiceDiagnosticConverter {

    public TimestampingServiceDiagnostics convert(DiagnosticsStatus diagnosticsStatus) {
        TimestampingServiceDiagnostics timestampingServiceDiagnostics = new TimestampingServiceDiagnostics();
        timestampingServiceDiagnostics.setUrl(diagnosticsStatus.getDescription());
        Optional<TimestampingStatus> statusCode = TimestampingStatusMapping.map(
                diagnosticsStatus.getReturnCode());
        timestampingServiceDiagnostics.setStatusCode(statusCode.orElse(null));
        Optional<DiagnosticStatusClass> statusClass = DiagnosticStatusClassMapping.map(
                diagnosticsStatus.getReturnCode());
        timestampingServiceDiagnostics.setStatusClass(statusClass.orElse(null));
        if (diagnosticsStatus.getPrevUpdate() != null) {
            timestampingServiceDiagnostics.setPrevUpdateAt(FormatUtils.fromLocalTimeToOffsetDateTime(
                    diagnosticsStatus.getPrevUpdate(), true));
        }

        return timestampingServiceDiagnostics;
    }

    public List<TimestampingServiceDiagnostics> convert(Iterable<DiagnosticsStatus> statuses)  {
        return Streams.stream(statuses)
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
