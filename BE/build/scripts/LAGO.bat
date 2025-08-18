@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  LAGO startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and LAGO_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\LAGO-1.0.0-SNAPSHOT-plain.jar;%APP_HOME%\lib\spring-boot-devtools-3.5.4.jar;%APP_HOME%\lib\spring-boot-starter-data-jpa-3.5.4.jar;%APP_HOME%\lib\spring-boot-starter-data-redis-3.5.4.jar;%APP_HOME%\lib\spring-boot-starter-websocket-3.5.4.jar;%APP_HOME%\lib\spring-boot-starter-web-3.5.4.jar;%APP_HOME%\lib\springdoc-openapi-starter-webmvc-ui-2.8.9.jar;%APP_HOME%\lib\springdoc-openapi-starter-webmvc-api-2.8.9.jar;%APP_HOME%\lib\springdoc-openapi-starter-common-2.8.9.jar;%APP_HOME%\lib\spring-boot-starter-validation-3.5.4.jar;%APP_HOME%\lib\jsoup-1.17.2.jar;%APP_HOME%\lib\kotlin-reflect-1.9.10.jar;%APP_HOME%\lib\spring-boot-starter-webflux-3.5.4.jar;%APP_HOME%\lib\spring-boot-starter-json-3.5.4.jar;%APP_HOME%\lib\swagger-core-jakarta-2.2.30.jar;%APP_HOME%\lib\swagger-models-jakarta-2.2.30.jar;%APP_HOME%\lib\jackson-annotations-2.19.2.jar;%APP_HOME%\lib\firebase-admin-9.2.0.jar;%APP_HOME%\lib\google-cloud-storage-2.22.4.jar;%APP_HOME%\lib\jackson-datatype-jdk8-2.19.2.jar;%APP_HOME%\lib\jackson-module-parameter-names-2.19.2.jar;%APP_HOME%\lib\jackson-dataformat-yaml-2.19.2.jar;%APP_HOME%\lib\jackson-core-2.19.2.jar;%APP_HOME%\lib\spring-boot-starter-actuator-3.5.4.jar;%APP_HOME%\lib\spring-boot-actuator-autoconfigure-3.5.4.jar;%APP_HOME%\lib\jackson-datatype-jsr310-2.19.2.jar;%APP_HOME%\lib\jjwt-jackson-0.11.5.jar;%APP_HOME%\lib\jackson-databind-2.19.2.jar;%APP_HOME%\lib\okhttp-4.12.0.jar;%APP_HOME%\lib\spring-boot-starter-oauth2-client-3.5.4.jar;%APP_HOME%\lib\spring-boot-starter-security-3.5.4.jar;%APP_HOME%\lib\jjwt-impl-0.11.5.jar;%APP_HOME%\lib\jjwt-api-0.11.5.jar;%APP_HOME%\lib\java-dotenv-5.2.2.jar;%APP_HOME%\lib\zstd-jni-1.5.6-10.jar;%APP_HOME%\lib\postgresql-42.7.7.jar;%APP_HOME%\lib\spring-boot-starter-jdbc-3.5.4.jar;%APP_HOME%\lib\spring-boot-starter-3.5.4.jar;%APP_HOME%\lib\spring-boot-autoconfigure-3.5.4.jar;%APP_HOME%\lib\spring-boot-actuator-3.5.4.jar;%APP_HOME%\lib\spring-boot-3.5.4.jar;%APP_HOME%\lib\hibernate-core-6.6.22.Final.jar;%APP_HOME%\lib\spring-data-jpa-3.5.2.jar;%APP_HOME%\lib\spring-aspects-6.2.9.jar;%APP_HOME%\lib\lettuce-core-6.6.0.RELEASE.jar;%APP_HOME%\lib\spring-data-redis-3.5.2.jar;%APP_HOME%\lib\spring-boot-starter-tomcat-3.5.4.jar;%APP_HOME%\lib\spring-webmvc-6.2.9.jar;%APP_HOME%\lib\spring-websocket-6.2.9.jar;%APP_HOME%\lib\spring-webflux-6.2.9.jar;%APP_HOME%\lib\spring-security-oauth2-client-6.5.2.jar;%APP_HOME%\lib\spring-security-web-6.5.2.jar;%APP_HOME%\lib\spring-security-oauth2-jose-6.5.2.jar;%APP_HOME%\lib\spring-security-oauth2-core-6.5.2.jar;%APP_HOME%\lib\spring-web-6.2.9.jar;%APP_HOME%\lib\spring-messaging-6.2.9.jar;%APP_HOME%\lib\tomcat-embed-el-10.1.43.jar;%APP_HOME%\lib\hibernate-validator-8.0.2.Final.jar;%APP_HOME%\lib\swagger-ui-5.21.0.jar;%APP_HOME%\lib\webjars-locator-lite-1.1.0.jar;%APP_HOME%\lib\okio-jvm-3.6.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.9.25.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.9.25.jar;%APP_HOME%\lib\kotlin-stdlib-1.9.25.jar;%APP_HOME%\lib\spring-boot-starter-reactor-netty-3.5.4.jar;%APP_HOME%\lib\spring-security-config-6.5.2.jar;%APP_HOME%\lib\spring-security-core-6.5.2.jar;%APP_HOME%\lib\spring-data-keyvalue-3.5.2.jar;%APP_HOME%\lib\spring-context-support-6.2.9.jar;%APP_HOME%\lib\spring-context-6.2.9.jar;%APP_HOME%\lib\spring-aop-6.2.9.jar;%APP_HOME%\lib\micrometer-jakarta9-1.15.2.jar;%APP_HOME%\lib\micrometer-core-1.15.2.jar;%APP_HOME%\lib\micrometer-observation-1.15.2.jar;%APP_HOME%\lib\google-api-client-gson-2.2.0.jar;%APP_HOME%\lib\google-api-client-2.2.0.jar;%APP_HOME%\lib\google-cloud-firestore-3.13.0.jar;%APP_HOME%\lib\google-auth-library-oauth2-http-1.17.0.jar;%APP_HOME%\lib\google-oauth-client-1.34.1.jar;%APP_HOME%\lib\google-http-client-gson-1.43.1.jar;%APP_HOME%\lib\google-http-client-apache-v2-1.43.1.jar;%APP_HOME%\lib\google-http-client-1.43.1.jar;%APP_HOME%\lib\proto-google-cloud-firestore-bundle-v1-3.13.0.jar;%APP_HOME%\lib\api-common-2.12.0.jar;%APP_HOME%\lib\opencensus-contrib-http-util-0.31.1.jar;%APP_HOME%\lib\guava-31.1-jre.jar;%APP_HOME%\lib\HikariCP-6.3.1.jar;%APP_HOME%\lib\spring-data-commons-3.5.2.jar;%APP_HOME%\lib\redis-authx-core-0.1.1-beta2.jar;%APP_HOME%\lib\spring-boot-starter-logging-3.5.4.jar;%APP_HOME%\lib\logback-classic-1.5.18.jar;%APP_HOME%\lib\log4j-to-slf4j-2.24.3.jar;%APP_HOME%\lib\jul-to-slf4j-2.0.17.jar;%APP_HOME%\lib\slf4j-api-2.0.17.jar;%APP_HOME%\lib\reactor-netty-http-1.2.8.jar;%APP_HOME%\lib\netty-codec-http2-4.1.123.Final.jar;%APP_HOME%\lib\reactor-netty-core-1.2.8.jar;%APP_HOME%\lib\netty-handler-proxy-4.1.123.Final.jar;%APP_HOME%\lib\netty-codec-http-4.1.123.Final.jar;%APP_HOME%\lib\netty-resolver-dns-native-macos-4.1.123.Final-osx-x86_64.jar;%APP_HOME%\lib\netty-resolver-dns-classes-macos-4.1.123.Final.jar;%APP_HOME%\lib\netty-resolver-dns-4.1.123.Final.jar;%APP_HOME%\lib\netty-handler-4.1.123.Final.jar;%APP_HOME%\lib\netty-codec-dns-4.1.123.Final.jar;%APP_HOME%\lib\netty-codec-socks-4.1.123.Final.jar;%APP_HOME%\lib\netty-codec-4.1.123.Final.jar;%APP_HOME%\lib\netty-transport-native-epoll-4.1.123.Final-linux-x86_64.jar;%APP_HOME%\lib\netty-transport-classes-epoll-4.1.123.Final.jar;%APP_HOME%\lib\netty-transport-native-unix-common-4.1.123.Final.jar;%APP_HOME%\lib\netty-transport-4.1.123.Final.jar;%APP_HOME%\lib\proto-google-cloud-firestore-v1-3.13.0.jar;%APP_HOME%\lib\checker-qual-3.49.3.jar;%APP_HOME%\lib\spring-orm-6.2.9.jar;%APP_HOME%\lib\spring-jdbc-6.2.9.jar;%APP_HOME%\lib\spring-tx-6.2.9.jar;%APP_HOME%\lib\spring-oxm-6.2.9.jar;%APP_HOME%\lib\spring-beans-6.2.9.jar;%APP_HOME%\lib\spring-expression-6.2.9.jar;%APP_HOME%\lib\spring-core-6.2.9.jar;%APP_HOME%\lib\jakarta.annotation-api-2.1.1.jar;%APP_HOME%\lib\snakeyaml-2.4.jar;%APP_HOME%\lib\jakarta.persistence-api-3.1.0.jar;%APP_HOME%\lib\jakarta.transaction-api-2.0.1.jar;%APP_HOME%\lib\jboss-logging-3.6.1.Final.jar;%APP_HOME%\lib\hibernate-commons-annotations-7.0.3.Final.jar;%APP_HOME%\lib\jandex-3.2.0.jar;%APP_HOME%\lib\classmate-1.7.0.jar;%APP_HOME%\lib\byte-buddy-1.17.6.jar;%APP_HOME%\lib\jaxb-runtime-4.0.5.jar;%APP_HOME%\lib\jaxb-core-4.0.5.jar;%APP_HOME%\lib\jakarta.xml.bind-api-4.0.2.jar;%APP_HOME%\lib\jakarta.inject-api-2.0.1.jar;%APP_HOME%\lib\antlr4-runtime-4.13.0.jar;%APP_HOME%\lib\aspectjweaver-1.9.24.jar;%APP_HOME%\lib\netty-buffer-4.1.123.Final.jar;%APP_HOME%\lib\netty-resolver-4.1.123.Final.jar;%APP_HOME%\lib\netty-common-4.1.123.Final.jar;%APP_HOME%\lib\reactor-core-3.7.8.jar;%APP_HOME%\lib\tomcat-embed-websocket-10.1.43.jar;%APP_HOME%\lib\tomcat-embed-core-10.1.43.jar;%APP_HOME%\lib\jakarta.validation-api-3.0.2.jar;%APP_HOME%\lib\jspecify-1.0.0.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\spring-security-crypto-6.5.2.jar;%APP_HOME%\lib\oauth2-oidc-sdk-9.43.6.jar;%APP_HOME%\lib\nimbus-jose-jwt-9.37.3.jar;%APP_HOME%\lib\micrometer-commons-1.15.2.jar;%APP_HOME%\lib\httpclient-4.5.14.jar;%APP_HOME%\lib\commons-codec-1.18.0.jar;%APP_HOME%\lib\httpcore-4.4.16.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\gson-2.13.1.jar;%APP_HOME%\lib\error_prone_annotations-2.38.0.jar;%APP_HOME%\lib\j2objc-annotations-1.3.jar;%APP_HOME%\lib\opencensus-api-0.31.1.jar;%APP_HOME%\lib\auto-value-annotations-1.10.1.jar;%APP_HOME%\lib\javax.annotation-api-1.3.2.jar;%APP_HOME%\lib\google-auth-library-credentials-1.17.0.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\google-http-client-jackson2-1.43.1.jar;%APP_HOME%\lib\google-api-services-storage-v1-rev20230301-2.0.0.jar;%APP_HOME%\lib\google-cloud-core-2.19.0.jar;%APP_HOME%\lib\google-cloud-core-http-2.19.0.jar;%APP_HOME%\lib\google-http-client-appengine-1.43.1.jar;%APP_HOME%\lib\gax-httpjson-0.114.0.jar;%APP_HOME%\lib\google-cloud-core-grpc-2.19.0.jar;%APP_HOME%\lib\gax-2.29.0.jar;%APP_HOME%\lib\gax-grpc-2.29.0.jar;%APP_HOME%\lib\grpc-alts-1.55.1.jar;%APP_HOME%\lib\grpc-grpclb-1.55.1.jar;%APP_HOME%\lib\conscrypt-openjdk-uber-2.5.2.jar;%APP_HOME%\lib\grpc-auth-1.55.1.jar;%APP_HOME%\lib\grpc-protobuf-1.55.1.jar;%APP_HOME%\lib\grpc-protobuf-lite-1.55.1.jar;%APP_HOME%\lib\grpc-context-1.55.1.jar;%APP_HOME%\lib\proto-google-iam-v1-1.15.0.jar;%APP_HOME%\lib\protobuf-java-3.23.2.jar;%APP_HOME%\lib\protobuf-java-util-3.23.2.jar;%APP_HOME%\lib\proto-google-common-protos-2.20.0.jar;%APP_HOME%\lib\threetenbp-1.6.8.jar;%APP_HOME%\lib\proto-google-cloud-storage-v2-2.22.4-alpha.jar;%APP_HOME%\lib\grpc-google-cloud-storage-v2-2.22.4-alpha.jar;%APP_HOME%\lib\gapic-google-cloud-storage-v2-2.22.4-alpha.jar;%APP_HOME%\lib\grpc-api-1.55.1.jar;%APP_HOME%\lib\grpc-netty-shaded-1.55.1.jar;%APP_HOME%\lib\perfmark-api-0.26.0.jar;%APP_HOME%\lib\grpc-core-1.55.1.jar;%APP_HOME%\lib\annotations-4.1.1.4.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.23.jar;%APP_HOME%\lib\grpc-stub-1.55.1.jar;%APP_HOME%\lib\grpc-googleapis-1.55.1.jar;%APP_HOME%\lib\grpc-xds-1.55.1.jar;%APP_HOME%\lib\opencensus-proto-0.2.0.jar;%APP_HOME%\lib\grpc-services-1.55.1.jar;%APP_HOME%\lib\re2j-1.6.jar;%APP_HOME%\lib\grpc-rls-1.55.1.jar;%APP_HOME%\lib\commons-logging-1.2.jar;%APP_HOME%\lib\opencensus-contrib-grpc-util-0.31.1.jar;%APP_HOME%\lib\spring-jcl-6.2.9.jar;%APP_HOME%\lib\angus-activation-2.0.2.jar;%APP_HOME%\lib\jakarta.activation-api-2.1.3.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar;%APP_HOME%\lib\jcip-annotations-1.0-1.jar;%APP_HOME%\lib\content-type-2.2.jar;%APP_HOME%\lib\json-smart-2.5.2.jar;%APP_HOME%\lib\lang-tag-1.7.jar;%APP_HOME%\lib\HdrHistogram-2.2.2.jar;%APP_HOME%\lib\LatencyUtils-2.0.3.jar;%APP_HOME%\lib\logback-core-1.5.18.jar;%APP_HOME%\lib\log4j-api-2.24.3.jar;%APP_HOME%\lib\txw2-4.0.5.jar;%APP_HOME%\lib\istack-commons-runtime-4.1.2.jar;%APP_HOME%\lib\commons-lang3-3.17.0.jar;%APP_HOME%\lib\swagger-annotations-jakarta-2.2.30.jar;%APP_HOME%\lib\accessors-smart-2.5.2.jar;%APP_HOME%\lib\asm-9.7.1.jar


@rem Execute LAGO
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %LAGO_OPTS%  -classpath "%CLASSPATH%" com.example.LAGO.LagoApplication %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable LAGO_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%LAGO_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
