# Implementation Plan - Refactor CajaViewModel and CajaRepository

Refactor `CajaViewModel` and `CajaRepository` to improve separation of concerns by moving HTTP response validation and exception handling from the ViewModel to the Repository. The ViewModel will then handle state based on a custom `CajaResult` sealed class.

## User Review Required

> [!IMPORTANT]
> The implementation of tests requires `kotlinx-coroutines-test` and a mocking library (like `MockK` or `Mockito`). I will add these to `libs.versions.toml` and `app/build.gradle.kts` if not present, to ensure the new tests can run correctly.

## Proposed Changes

### Caja Component

#### [MODIFY] [CajaRepository.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/CajaRepository.kt)
- Define `CajaResult` sealed class to represent success and error states.
- Update `CajaRepository` interface to return `CajaResult` instead of `Response<DataCajaResponse>`.
- Update `CajaRepositoryImpl` to handle API calls, response validation (success, null body, empty list), and exceptions (`IOException`, `HttpException`).

#### [MODIFY] [CajaViewModel.kt](file:///C:/Users/marco/SodAppComposse/app/src/main/java/com/example/sodappcomposse/Caja/CajaViewModel.kt)
- Simplify `getCajaPorMes()` by removing nested `if` and `try-catch` blocks.
- Use `when` to handle `CajaResult` from the repository.
- Ensure the ViewModel only manages `CajaUiState` and updates the `_caja` flow.

### Dependencies

#### [MODIFY] [libs.versions.toml](file:///C:/Users/marco/SodAppComposse/gradle/libs.versions.toml)
- Add versions and libraries for `kotlinx-coroutines-test` and `mockk`.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/marco/SodAppComposse/app/build.gradle.kts)
- Add `testImplementation` for the new testing libraries.

### Tests

#### [NEW] [CajaRepositoryImplTest.kt](file:///C:/Users/marco/SodAppComposse/app/src/test/java/com/example/sodappcomposse/Caja/CajaRepositoryImplTest.kt)
- Unit tests for `CajaRepositoryImpl` covering:
    - Success with data.
    - Success with empty list (error state for this app).
    - Null body.
    - API error (404, 500).
    - Network exceptions.

#### [NEW] [CajaViewModelTest.kt](file:///C:/Users/marco/SodAppComposse/app/src/test/java/com/example/sodappcomposse/Caja/CajaViewModelTest.kt)
- Unit tests for `CajaViewModel` covering state transitions (Idle -> Loading -> Success/Error) based on repository results.

## Verification Plan

### Automated Tests
- Run `./gradlew test` to execute the new unit tests.
- Specifically:
    - `CajaRepositoryImplTest`
    - `CajaViewModelTest`

### Manual Verification
- Deploy the app and navigate to the "Caja" section.
- Select different months and verify that data loads correctly or displays appropriate error messages.
