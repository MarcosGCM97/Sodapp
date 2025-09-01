package com.example.sodappcomposse.datos

import androidx.lifecycle.ViewModel
import com.example.sodappcomposse.API.ApiServices
import com.example.sodappcomposse.API.RetrofitInstance
import com.example.sodappcomposse.Cliente.Cliente
import com.example.sodappcomposse.Producto.Producto
import com.example.sodappcomposse.Ventas.DataVenta


class DatosViewModel (
    apiServices: ApiServices = RetrofitInstance.api
): ViewModel(){

    var _productosDatos : MutableList<Producto> = mutableListOf()
    val productosDatos :List<Producto> = _productosDatos

    var _clientesDatos : MutableList<Cliente> = mutableListOf()
    val clientesDatos :List<Cliente> = _clientesDatos

    var _ventasDatos : MutableList<DataVenta> = mutableListOf()
    val ventasDatos :List<DataVenta> = _ventasDatos

    fun cargarDatos(productos: List<Producto>, clientes: List<Cliente>, ventas: List<DataVenta>){

        _productosDatos.clear()
        _productosDatos.addAll(productos)

        _clientesDatos.clear()
        _clientesDatos.addAll(clientes)

        _ventasDatos.clear()
        _ventasDatos.addAll(ventas)
    }

}