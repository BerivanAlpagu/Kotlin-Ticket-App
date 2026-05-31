package com.turkcell.ticketpass.di

import com.turkcell.ticketpass.viewmodel.EventDetailViewModel
import com.turkcell.ticketpass.viewmodel.LoginViewModel
import com.turkcell.ticketpass.viewmodel.RegisterViewModel
import com.turkcell.ticketpass.viewmodel.HomeViewModel
import com.turkcell.ticketpass.viewmodel.MyTicketsViewModel
import com.turkcell.ticketpass.viewmodel.TicketDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::EventDetailViewModel)
    viewModelOf(::MyTicketsViewModel)
    viewModelOf(::TicketDetailViewModel)
}