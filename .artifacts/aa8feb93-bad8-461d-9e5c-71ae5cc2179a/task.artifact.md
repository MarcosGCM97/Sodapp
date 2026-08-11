# Tasks - Refactor CajaViewModel and CajaRepository

- [x] Update testing dependencies
    - [x] Add `kotlinx-coroutines-test` and `mockk` to `libs.versions.toml`
    - [x] Add `testImplementation` to `app/build.gradle.kts`
    - [x] Gradle Sync
- [/] Refactor CajaRepository
    - [ ] Define `CajaResult` in `CajaRepository.kt`
    - [ ] Update `CajaRepository` interface
    - [ ] Update `CajaRepositoryImpl` with validation logic
- [ ] Refactor CajaViewModel
    - [ ] Simplify `getCajaPorMes` logic
    - [ ] Handle `CajaResult` using `when`
- [ ] Implement Unit Tests
    - [ ] Create `CajaRepositoryImplTest.kt`
    - [ ] Create `CajaViewModelTest.kt`
- [ ] Verification
    - [ ] Run unit tests
    - [ ] Manual verification (if possible)
