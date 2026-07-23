package com.example.sodappcomposse.di

import com.example.sodappcomposse.Caja.CajaRepository
import com.example.sodappcomposse.Caja.CajaRepositoryImpl
import com.example.sodappcomposse.Cliente.AgendaRepository
import com.example.sodappcomposse.Cliente.AgendaRepositoryImpl
import com.example.sodappcomposse.Cliente.ClienteRepository
import com.example.sodappcomposse.Cliente.ClienteRepositoryImpl
import com.example.sodappcomposse.IngresoUsuario.AuthRepository
import com.example.sodappcomposse.IngresoUsuario.AuthRepositoryImpl
import com.example.sodappcomposse.Producto.ProductoRepository
import com.example.sodappcomposse.Producto.ProductoRepositoryImpl
import com.example.sodappcomposse.Ventas.VentaRepository
import com.example.sodappcomposse.Ventas.VentaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindClienteRepository(impl: ClienteRepositoryImpl): ClienteRepository

    @Binds
    @Singleton
    abstract fun bindProductoRepository(impl: ProductoRepositoryImpl): ProductoRepository

    @Binds
    @Singleton
    abstract fun bindVentaRepository(impl: VentaRepositoryImpl): VentaRepository

    @Binds
    @Singleton
    abstract fun bindCajaRepository(impl: CajaRepositoryImpl): CajaRepository

    @Binds
    @Singleton
    abstract fun bindAgendaRepository(impl: AgendaRepositoryImpl): AgendaRepository
}
