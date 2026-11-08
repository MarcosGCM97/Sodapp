package com.example.sodappcomposse.Administracion

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sodappcomposse.Componentes.ScreenWithBackButtonWrapper
import com.example.sodappcomposse.R
import com.example.sodappcomposse.auth.LocalSessionPermissions
import com.example.sodappcomposse.auth.puedeAdministrar

/** Owner-only screen: company users (tab Usuarios) and roles with their permissions (tab Roles). */
@Composable
fun AdministracionScreen(
    navController: NavController,
    viewModel: AdministracionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val esDueno = puedeAdministrar(LocalSessionPermissions.current)
    // 0 = Usuarios, 1 = Roles
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(esDueno) {
        if (esDueno) viewModel.cargar()
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AdministracionUiState.Success -> {
                Toast.makeText(context, context.getString(state.messageRes), Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is AdministracionUiState.Error -> {
                // Backend messages (400/404/409), the 403 message or a network error
                Toast.makeText(context, context.getString(state.messageRes, *state.args), Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    ScreenWithBackButtonWrapper(navController = navController, title = stringResource(R.string.administracion_titulo)) {
        if (!esDueno) {
            Text(stringResource(R.string.error_sin_permiso))
            return@ScreenWithBackButtonWrapper
        }

        TabRow(selectedTabIndex = pestana) {
            listOf(stringResource(R.string.tab_usuarios), stringResource(R.string.tab_roles)).forEachIndexed { index, titulo ->
                Tab(
                    selected = pestana == index,
                    onClick = { pestana = index },
                    text = { Text(titulo) }
                )
            }
        }

        val enCurso = uiState is AdministracionUiState.Loading
        if (enCurso) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            Spacer(Modifier.padding(top = 4.dp))
        }

        when (pestana) {
            0 -> UsuariosTab(
                usuarios = viewModel.usuarios.value,
                roles = viewModel.roles.value,
                enCurso = enCurso,
                onCrear = viewModel::crearUsuario,
                onCambiarRol = viewModel::cambiarRol,
                onCambiarContrasena = viewModel::cambiarContrasena,
                onCambiarActivo = viewModel::cambiarActivo
            )
            else -> RolesTab(
                roles = viewModel.roles.value,
                permisosDisponibles = viewModel.permisosDisponibles.value,
                enCurso = enCurso,
                onGuardar = { rol, nombre, permisos ->
                    if (rol == null) viewModel.crearRol(nombre, permisos) else viewModel.actualizarRol(rol.id, nombre, permisos)
                },
                onEliminar = { viewModel.eliminarRol(it.id) }
            )
        }
    }
}

@Composable
private fun UsuariosTab(
    usuarios: List<UsuarioAdmin>,
    roles: List<RolAdmin>,
    enCurso: Boolean,
    onCrear: (String, String, Int) -> Unit,
    onCambiarRol: (Int, Int) -> Unit,
    onCambiarContrasena: (Int, String) -> Unit,
    onCambiarActivo: (Int, Boolean) -> Unit
) {
    var creando by remember { mutableStateOf(false) }
    var cambiandoRol by remember { mutableStateOf<UsuarioAdmin?>(null) }
    var cambiandoContrasena by remember { mutableStateOf<UsuarioAdmin?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Button(
                onClick = { creando = true },
                enabled = roles.isNotEmpty() && !enCurso,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.nuevo_usuario))
            }
            if (roles.isEmpty()) {
                Text(stringResource(R.string.crear_rol_primero), style = MaterialTheme.typography.bodySmall)
            }
        }
        if (usuarios.isEmpty()) {
            item { Text(stringResource(R.string.sin_usuarios)) }
        }
        items(usuarios, key = { it.id }) { usuario ->
            UsuarioCard(
                usuario = usuario,
                enCurso = enCurso,
                onCambiarRol = { cambiandoRol = usuario },
                onCambiarContrasena = { cambiandoContrasena = usuario },
                onCambiarActivo = { onCambiarActivo(usuario.id, it) }
            )
        }
    }

    if (creando) {
        UsuarioNuevoDialog(
            roles = roles,
            onCrear = { nombre, contrasena, rolId ->
                onCrear(nombre, contrasena, rolId)
                creando = false
            },
            onDismiss = { creando = false }
        )
    }

    cambiandoRol?.let { usuario ->
        var rolId by remember(usuario.id) { mutableStateOf(usuario.rolId) }
        AlertDialog(
            onDismissRequest = { cambiandoRol = null },
            title = { Text(stringResource(R.string.cambiar_rol_label)) },
            text = {
                Column {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                    SelectorRol(roles = roles, seleccionado = rolId, onSeleccionar = { rolId = it })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        rolId?.let { onCambiarRol(usuario.id, it) }
                        cambiandoRol = null
                    },
                    enabled = rolId != null && rolId != usuario.rolId
                ) { Text(stringResource(R.string.guardar)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { cambiandoRol = null }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }

    cambiandoContrasena?.let { usuario ->
        var contrasena by remember(usuario.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { cambiandoContrasena = null },
            title = { Text(stringResource(R.string.cambiar_contrasena_label)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
                    CampoContrasena(
                        valor = contrasena,
                        onCambiar = { contrasena = it },
                        etiqueta = stringResource(R.string.nueva_contrasena_label)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCambiarContrasena(usuario.id, contrasena)
                        cambiandoContrasena = null
                    },
                    enabled = contrasena.isNotBlank()
                ) { Text(stringResource(R.string.guardar)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { cambiandoContrasena = null }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }
}

@Composable
private fun UsuarioCard(
    usuario: UsuarioAdmin,
    enCurso: Boolean,
    onCambiarRol: () -> Unit,
    onCambiarContrasena: () -> Unit,
    onCambiarActivo: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = when {
                    usuario.esDueno -> stringResource(R.string.dueno_label)
                    else -> usuario.rolNombre ?: stringResource(R.string.sin_rol_label)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            // The owner's own row cannot be changed (the backend rejects it too)
            if (!usuario.esDueno) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(if (usuario.estaActivo) R.string.usuario_activo_label else R.string.usuario_inactivo_label),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = usuario.estaActivo, onCheckedChange = onCambiarActivo, enabled = !enCurso)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onCambiarRol, enabled = !enCurso) {
                        Text(stringResource(R.string.cambiar_rol_label))
                    }
                    TextButton(onClick = onCambiarContrasena, enabled = !enCurso) {
                        Text(stringResource(R.string.cambiar_contrasena_label))
                    }
                }
            }
        }
    }
}

@Composable
private fun UsuarioNuevoDialog(
    roles: List<RolAdmin>,
    onCrear: (String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var rolId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nuevo_usuario)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.nombre_usuario_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                CampoContrasena(
                    valor = contrasena,
                    onCambiar = { contrasena = it },
                    etiqueta = stringResource(R.string.contrasena_label)
                )
                Text(stringResource(R.string.rol_label), style = MaterialTheme.typography.titleSmall)
                SelectorRol(roles = roles, seleccionado = rolId, onSeleccionar = { rolId = it })
            }
        },
        confirmButton = {
            Button(
                onClick = { rolId?.let { onCrear(nombre, contrasena, it) } },
                enabled = nombre.isNotBlank() && contrasena.isNotBlank() && rolId != null
            ) { Text(stringResource(R.string.guardar)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}

@Composable
private fun CampoContrasena(valor: String, onCambiar: (String) -> Unit, etiqueta: String) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambiar,
        label = { Text(etiqueta) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}

/** One radio button per role. */
@Composable
private fun SelectorRol(roles: List<RolAdmin>, seleccionado: Int?, onSeleccionar: (Int) -> Unit) {
    Column {
        roles.forEach { rol ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = rol.id == seleccionado,
                        onClick = { onSeleccionar(rol.id) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = rol.id == seleccionado, onClick = null)
                Text(rol.nombre, modifier = Modifier.padding(start = 12.dp))
            }
        }
    }
}

@Composable
private fun RolesTab(
    roles: List<RolAdmin>,
    permisosDisponibles: List<PermisoDisponible>,
    enCurso: Boolean,
    // rol null = new role
    onGuardar: (RolAdmin?, String, List<String>) -> Unit,
    onEliminar: (RolAdmin) -> Unit
) {
    // Role being edited in the dialog; a blank RolAdmin (id 0) stands for a new role
    var editando by remember { mutableStateOf<RolAdmin?>(null) }
    var eliminando by remember { mutableStateOf<RolAdmin?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Button(
                onClick = { editando = RolAdmin() },
                enabled = !enCurso,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.nuevo_rol))
            }
        }
        if (roles.isEmpty()) {
            item { Text(stringResource(R.string.sin_roles)) }
        }
        items(roles, key = { it.id }) { rol ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(rol.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.usuarios_rol_format, rol.usuarios), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(R.string.permisos_rol_format, rol.permisos.orEmpty().size),
                            style = MaterialTheme.typography.bodySmall
                        )
                        val descripciones = permisosDisponibles
                            .filter { it.codigo in rol.permisos.orEmpty() }
                            .joinToString { it.descripcion }
                        if (descripciones.isNotEmpty()) {
                            Text(descripciones, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    IconButton(onClick = { editando = rol }, enabled = !enCurso) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.editar_desc))
                    }
                    IconButton(onClick = { eliminando = rol }, enabled = !enCurso) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.eliminar_desc))
                    }
                }
            }
        }
    }

    editando?.let { rol ->
        RolDialog(
            inicial = rol,
            permisosDisponibles = permisosDisponibles,
            onGuardar = { nombre, permisos ->
                onGuardar(rol.takeIf { it.id != 0 }, nombre, permisos)
                editando = null
            },
            onDismiss = { editando = null }
        )
    }

    eliminando?.let { rol ->
        AlertDialog(
            onDismissRequest = { eliminando = null },
            title = { Text(stringResource(R.string.confirmar_eliminacion_titulo)) },
            text = { Text(stringResource(R.string.confirmar_eliminar_rol_msg, rol.nombre)) },
            confirmButton = {
                Button(
                    onClick = {
                        // A role with users is rejected by the backend (409) and its message is shown
                        onEliminar(rol)
                        eliminando = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.eliminar_label)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { eliminando = null }) { Text(stringResource(R.string.cancelar)) }
            }
        )
    }
}

/** Name and permission checkboxes of a role ([inicial] with id 0 is a new role). */
@Composable
private fun RolDialog(
    inicial: RolAdmin,
    permisosDisponibles: List<PermisoDisponible>,
    onGuardar: (String, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(inicial.nombre) }
    var marcados by remember { mutableStateOf(inicial.permisos.orEmpty().toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (inicial.id == 0) R.string.nuevo_rol else R.string.editar_rol)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.nombre_rol_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.permisos_label), style = MaterialTheme.typography.titleSmall)
                Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                    permisosDisponibles.forEach { permiso ->
                        val marcado = permiso.codigo in marcados
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = marcado,
                                    onValueChange = { marcados = if (it) marcados + permiso.codigo else marcados - permiso.codigo },
                                    role = Role.Checkbox
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = marcado, onCheckedChange = null)
                            Text(permiso.descripcion, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                // Keep the catalog order so the request is stable
                onClick = { onGuardar(nombre, permisosDisponibles.map { it.codigo }.filter { it in marcados }) },
                enabled = nombre.isNotBlank()
            ) { Text(stringResource(R.string.guardar)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar)) }
        }
    )
}
