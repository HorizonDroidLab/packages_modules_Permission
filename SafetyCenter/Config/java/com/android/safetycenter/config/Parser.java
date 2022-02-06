/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.safetycenter.config;

import android.annotation.IdRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.res.Resources;

import com.android.safetycenter.config.parser.XmlParser;

import java.io.InputStream;
import java.util.List;

/** Utility class to parse and validate a Safety Center Config */
public final class Parser {
    private Parser() {
    }

    /** Thrown when there is an error parsing the Safety Center Config */
    public static final class ParseException extends Exception {
        public ParseException(@NonNull String message) {
            super(message);
        }

        public ParseException(@NonNull String message, @NonNull Throwable ex) {
            super(message, ex);
        }
    }

    /**
     * Parses and validates the given raw XML resource into a {@link SafetyCenterConfig} object.
     *
     * <p>This method uses the XML parser auto generated by the xsdc tool from the
     * safety_center_config.xsd schema file and then applies extra validation on top of it.
     *
     * @param in              the raw XML resource representing the Safety Center configuration
     * @param resourcePkgName the name of the package that contains the Safety Center configuration
     * @param resources       the {@link Resources} retrieved from the package that contains the
     *                        Safety Center configuration
     */
    @Nullable
    public static SafetyCenterConfig parse(@NonNull InputStream in, @NonNull String resourcePkgName,
            @NonNull Resources resources) throws ParseException {
        validateInput(in, resourcePkgName, resources);
        com.android.safetycenter.config.parser.SafetyCenterConfig safetyCenterConfig;
        try {
            safetyCenterConfig = XmlParser.read(in);
        } catch (Exception e) {
            throw new ParseException("Exception while reading XML", e);
        }
        return convert(safetyCenterConfig, resourcePkgName, resources);
    }

    @NonNull
    static SafetyCenterConfig convert(
            @Nullable com.android.safetycenter.config.parser.SafetyCenterConfig
                    parserSafetyCenterConfig,
            @NonNull String resourcePkgName, @NonNull Resources resources)
            throws ParseException {
        if (parserSafetyCenterConfig == null) {
            throw new ParseException("Element safety-center-config missing");
        }
        com.android.safetycenter.config.parser.SafetySourcesConfig parserSafetySourcesConfig =
                parserSafetyCenterConfig.getSafetySourcesConfig();
        if (parserSafetySourcesConfig == null) {
            throw new ParseException("Element safety-sources-config missing");
        }
        SafetyCenterConfig.Builder builder = new SafetyCenterConfig.Builder();
        if (parserSafetySourcesConfig.getSafetySourcesGroup() == null
                || parserSafetySourcesConfig.getStaticSafetySourcesGroup() == null) {
            throw new ParseException("Element safety-sources-config invalid");
        }
        int safetySourcesGroupSize = parserSafetySourcesConfig.getSafetySourcesGroup().size();
        for (int i = 0; i < safetySourcesGroupSize; i++) {
            com.android.safetycenter.config.parser.SafetySourcesGroup parserSafetySourcesGroup =
                    parserSafetySourcesConfig.getSafetySourcesGroup().get(i);
            builder.addSafetySourcesGroup(
                    convert(parserSafetySourcesGroup, resourcePkgName, resources));
        }
        int staticSafetySourcesGroupSize =
                parserSafetySourcesConfig.getStaticSafetySourcesGroup().size();
        for (int i = 0; i < staticSafetySourcesGroupSize; i++) {
            com.android.safetycenter.config.parser.StaticSafetySourcesGroup
                    parserStaticSafetySourcesGroup =
                    parserSafetySourcesConfig.getStaticSafetySourcesGroup().get(i);
            builder.addStaticSafetySourcesGroup(
                    convert(parserStaticSafetySourcesGroup, resourcePkgName, resources));
        }
        try {
            return builder.build();
        } catch (IllegalStateException e) {
            throw new ParseException("Element safety-sources-config invalid", e);
        }
    }

    @NonNull
    static SafetySourcesGroup convert(
            @Nullable com.android.safetycenter.config.parser.SafetySourcesGroup
                    parserSafetySourcesGroup,
            @NonNull String resourcePkgName, @NonNull Resources resources)
            throws Parser.ParseException {
        if (parserSafetySourcesGroup == null) {
            throw new Parser.ParseException("Element safety-sources-group invalid");
        }
        SafetySourcesGroup.Builder builder = new SafetySourcesGroup.Builder();
        builder.setId(parserSafetySourcesGroup.getId());
        if (parserSafetySourcesGroup.getTitle() != null) {
            builder.setTitleResId(
                    parseReference(parserSafetySourcesGroup.getTitle(), resourcePkgName, resources,
                            "safety-sources-group", "title"));
        }
        if (parserSafetySourcesGroup.getSummary() != null) {
            builder.setSummaryResId(
                    parseReference(parserSafetySourcesGroup.getSummary(), resourcePkgName,
                            resources, "safety-sources-group", "summary"));
        }
        List<com.android.safetycenter.config.parser.SafetySource> parserSafetySourceList =
                parserSafetySourcesGroup.getSafetySource();
        int parserSafetySourceListSize = parserSafetySourceList.size();
        for (int i = 0; i < parserSafetySourceListSize; i++) {
            com.android.safetycenter.config.parser.SafetySource parserSafetySource =
                    parserSafetySourceList.get(i);
            builder.addSafetySource(
                    convert(parserSafetySource, resourcePkgName, resources, "safety-source"));
        }
        try {
            return builder.build();
        } catch (IllegalStateException e) {
            throw new ParseException("Element safety-sources-group invalid", e);
        }
    }

    @NonNull
    static StaticSafetySourcesGroup convert(
            @Nullable com.android.safetycenter.config.parser.StaticSafetySourcesGroup
                    parserStaticSafetySourcesGroup,
            @NonNull String resourcePkgName, @NonNull Resources resources)
            throws Parser.ParseException {
        if (parserStaticSafetySourcesGroup == null) {
            throw new Parser.ParseException("Element static-safety-sources-group invalid");
        }
        StaticSafetySourcesGroup.Builder builder = new StaticSafetySourcesGroup.Builder();
        builder.setId(parserStaticSafetySourcesGroup.getId());
        if (parserStaticSafetySourcesGroup.getTitle() != null) {
            builder.setTitleResId(
                    parseReference(parserStaticSafetySourcesGroup.getTitle(), resourcePkgName,
                            resources, "static-safety-sources-group", "title"));
        }
        List<com.android.safetycenter.config.parser.SafetySource> parserStaticSafetySourceList =
                parserStaticSafetySourcesGroup.getStaticSafetySource();
        int parserStaticSafetySourceListSize = parserStaticSafetySourceList.size();
        for (int i = 0; i < parserStaticSafetySourceListSize; i++) {
            com.android.safetycenter.config.parser.SafetySource parserSafetySource =
                    parserStaticSafetySourceList.get(i);
            builder.addStaticSafetySource(
                    convert(parserSafetySource, resourcePkgName, resources,
                            "static-safety-source"));
        }
        try {
            return builder.build();
        } catch (IllegalStateException e) {
            throw new ParseException("Element static-safety-sources-group invalid", e);
        }
    }

    @NonNull
    static SafetySource convert(
            @Nullable com.android.safetycenter.config.parser.SafetySource parserSafetySource,
            @NonNull String resourcePkgName, @NonNull Resources resources, @NonNull String name)
            throws Parser.ParseException {
        if (parserSafetySource == null) {
            throw new Parser.ParseException(String.format("Element %s invalid", name));
        }
        SafetySource.Builder builder = new SafetySource.Builder();
        if (parserSafetySource.getType() != 0) {
            builder.setType(parserSafetySource.getType());
        }
        builder.setId(parserSafetySource.getId());
        builder.setPackageName(parserSafetySource.getPackageName());
        if (parserSafetySource.getTitle() != null) {
            builder.setTitleResId(
                    parseReference(parserSafetySource.getTitle(), resourcePkgName, resources, name,
                            "title"));
        }
        if (parserSafetySource.getSummary() != null) {
            builder.setSummaryResId(
                    parseReference(parserSafetySource.getSummary(), resourcePkgName, resources,
                            name, "summary"));
        }
        builder.setIntentAction(parserSafetySource.getIntentAction());
        if (parserSafetySource.getProfile() != 0) {
            builder.setProfile(parserSafetySource.getProfile());
        }
        if (parserSafetySource.getSearchTerms() != null) {
            builder.setSearchTermsResId(
                    parseReference(parserSafetySource.getSearchTerms(), resourcePkgName, resources,
                            name, "searchTerms"));
        }
        builder.setBroadcastReceiverClassName(parserSafetySource.getBroadcastReceiverClassName());
        if (parserSafetySource.isDisallowLogging()) {
            builder.setDisallowLogging(parserSafetySource.isDisallowLogging());
        }
        if (parserSafetySource.isAllowRefreshOnPageOpen()) {
            builder.setAllowRefreshOnPageOpen(parserSafetySource.isAllowRefreshOnPageOpen());
        }
        try {
            return builder.build();
        } catch (IllegalStateException e) {
            throw new ParseException(String.format("Element %s invalid", name), e);
        }
    }

    static void validateInput(@NonNull InputStream in, @NonNull String resourcePkgName,
            @NonNull Resources resources) throws ParseException {
        if (in == null) {
            throw new ParseException("Input stream must be defined");
        }
        if (resourcePkgName == null || resourcePkgName.isEmpty()) {
            throw new ParseException("Resource package name must be defined");
        }
        if (resources == null) {
            throw new ParseException("Resources must be defined");
        }
    }

    @IdRes
    static int parseReference(@NonNull String reference, @NonNull String resourcePkgName,
            @NonNull Resources resources, @NonNull String parent, @NonNull String name)
            throws ParseException {
        if (!reference.startsWith("@string/")) {
            throw new ParseException(
                    String.format("String %s in %s.%s is not a reference", reference, parent,
                            name));
        }
        int id = resources.getIdentifier(reference.substring(1), null, resourcePkgName);
        if (id == Resources.ID_NULL) {
            throw new ParseException(
                    String.format("Reference %s in %s.%s missing", reference, parent, name));
        }
        return id;
    }

}
