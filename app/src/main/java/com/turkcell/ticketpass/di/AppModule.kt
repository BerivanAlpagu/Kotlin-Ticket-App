package com.turkcell.ticketpass.di

import com.turkcell.ticketpass.viewmodel.EventDetailViewModel
import com.turkcell.ticketpass.viewmodel.LoginViewModel
import com.turkcell.ticketpass.viewmodel.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.turkcell.ticketpass.viewmodel.HomeViewModel

val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::EventDetailViewModel)
}