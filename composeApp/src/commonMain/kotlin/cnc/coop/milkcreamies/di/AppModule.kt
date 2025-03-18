/*
 * Copyright 2025  MkaoCodes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cnc.coop.milkcreamies.di

import cnc.coop.milkcreamies.data.InventoryManager
import cnc.coop.milkcreamies.data.remote.MilkClient
import cnc.coop.milkcreamies.data.repository.CowRepositoryImpl
import cnc.coop.milkcreamies.data.repository.CowSummaryRepositoryImpl
import cnc.coop.milkcreamies.data.repository.CustomerRepositoryImpl
import cnc.coop.milkcreamies.data.repository.EarningsSummaryRepositoryImpl
import cnc.coop.milkcreamies.data.repository.MemberRepositoryImpl
import cnc.coop.milkcreamies.data.repository.MilkInEntryRepositoryImpl
import cnc.coop.milkcreamies.data.repository.MilkOutEntryRepositoryImpl
import cnc.coop.milkcreamies.data.repository.MilkSpoiltEntryRepositoryImpl
import cnc.coop.milkcreamies.data.repository.StockSummaryRepositoryImpl
import cnc.coop.milkcreamies.domain.repository.CowRepository
import cnc.coop.milkcreamies.domain.repository.CowSummaryRepository
import cnc.coop.milkcreamies.domain.repository.CustomerRepository
import cnc.coop.milkcreamies.domain.repository.EarningsSummaryRepository
import cnc.coop.milkcreamies.domain.repository.MemberRepository
import cnc.coop.milkcreamies.domain.repository.MilkInEntryRepository
import cnc.coop.milkcreamies.domain.repository.MilkOutEntryRepository
import cnc.coop.milkcreamies.domain.repository.MilkSpoiltEntryRepository
import cnc.coop.milkcreamies.domain.repository.StockSummaryRepository
import cnc.coop.milkcreamies.presentation.viewmodel.auth.AuthViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.cows.CowsViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.dashboard.DashboardViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.earnings.EarningsViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.members.MembersViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkInViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkOutViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.milk.MilkSpoiltViewModel
import cnc.coop.milkcreamies.presentation.viewmodel.stock.StockViewModel
import org.koin.dsl.module

val appModule = module {
    // Data layer
    single { InventoryManager() }
    single { MilkClient() }


    // Repositories
    single<MilkInEntryRepository> { MilkInEntryRepositoryImpl(get()) }
    single<CowRepository> { CowRepositoryImpl(get()) }
    single<MemberRepository> { MemberRepositoryImpl(get()) }
    single<MilkOutEntryRepository> { MilkOutEntryRepositoryImpl(get()) }
    single<MilkSpoiltEntryRepository> { MilkSpoiltEntryRepositoryImpl(get()) }
    single<CustomerRepository> { CustomerRepositoryImpl(get()) }
    single<EarningsSummaryRepository> { EarningsSummaryRepositoryImpl(get()) }
    single<StockSummaryRepository> { StockSummaryRepositoryImpl(get()) }
    single<CowSummaryRepository> { CowSummaryRepositoryImpl(get()) }

    // ViewModels
    single { AuthViewModel() }
    single {
        DashboardViewModel(
            get(), get(), get(), get(), get(), get(), get(),
            get(), get()
        )
    }
    single { CowsViewModel(get(), get()) }
    single { MilkInViewModel(get(), get(), get(), get()) }
    single { MilkOutViewModel(get(), get(), get()) }
    single { MilkSpoiltViewModel(get()) }
    single { StockViewModel(get(), get(), get(), get(), get(), get(), get()) }
    single { EarningsViewModel(get(), get(), get()) }
    single { MembersViewModel(get(), get(), get()) }
}
