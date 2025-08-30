# Code Quality and Standards

### 1. SonarCloud Configuration
```xml
<properties>
    <sonar.exclusions>
        src/main/resources/static/**,
        src/main/resources/public/**
    </sonar.exclusions>
    <sonar.inclusions>
        **/*.java,
        **/*.properties,
        **/*.xml,
        **/*.html
    </sonar.inclusions>
    <sonar.sources>src/main/java</sonar.sources>
    <sonar.tests>src/test/java</sonar.tests>
</properties>
```

### 2. JaCoCo Coverage Configuration
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/AplikasiMinibankApplication.class</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/dto/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

### 3. Test Naming Conventions
```java
// Unit test naming: should[ExpectedBehavior]When[StateUnderTest]
void shouldReturnTrueWhenAccountIsActive()
void shouldThrowExceptionWhenAmountIsNegative()

// Integration test naming: should[ExpectedOutcome][Context]
void shouldSaveAndFindAccountFromCsv()
void shouldFindAccountsByCustomer()

// Playwright test naming: should[UserAction][Expected Result]
void shouldLoginSuccessfullyWithValidCredentials()
void shouldDisplayErrorMessageWithInvalidCredentials()
```
