package ru.agimate.android.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ru.agimate.android.domain.usecase.SendTriggerUseCase
import ru.agimate.android.domain.usecase.TestConnectionUseCase
import ru.agimate.android.service.ActionServiceManager
import ru.agimate.android.service.TriggerServiceManager
import ru.agimate.android.ui.screens.actions.ActionsViewModel
import ru.agimate.android.ui.screens.settings.SettingsViewModel
import ru.agimate.android.ui.screens.triggers.TriggersViewModel
import ru.agimate.android.util.PermissionHelper

val appModule = module {
    // UseCases
    single { SendTriggerUseCase(get(), get(), get()) }
    single { TestConnectionUseCase(get()) }

    // Service
    single { TriggerServiceManager(androidContext(), get()) }
    single { ActionServiceManager(androidContext(), get()) }

    // ViewModels
    viewModel { ActionsViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { TriggersViewModel(get(), get(), get()) }

    // Utilities
    single { PermissionHelper(androidContext()) }
}
