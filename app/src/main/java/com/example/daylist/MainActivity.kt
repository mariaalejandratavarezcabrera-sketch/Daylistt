
package com.example.daylist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.daylist.ui.theme.DaylistTheme
import kotlinx.coroutines.launch
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val description: String,
    val completed: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DaylistTheme {
                DaylistApp()
            }
        }
    }
}

@Composable
fun DaylistApp() {

    val navController = rememberNavController()
    val context = LocalContext.current
    val storage = remember { TaskStorage(context) }

    val tasks by storage.tasks.collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            TaskListScreen(
                tasks = tasks,
                onAddClick = {
                    navController.navigate("add")
                },
                onTaskClick = { task ->
                    navController.navigate("detail/${task.id}")
                }
            )
        }

        composable("add") {
            AddTaskScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSave = { title, subject, description ->

                    scope.launch {
                        val newTask = Task(
                            title = title,
                            subject = subject,
                            description = description
                        )

                        storage.saveTasks(tasks + newTask)

                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = "detail/{taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->

            val taskId = entry.arguments?.getString("taskId")
            val task = tasks.find { it.id == taskId }

            if (task != null) {

                TaskDetailScreen(
                    task = task,

                    onBack = {
                        navController.popBackStack()
                    },

                    onCompleteChange = { completed ->

                        scope.launch {
                            val updatedTasks = tasks.map {
                                if (it.id == task.id) {
                                    it.copy(completed = completed)
                                } else {
                                    it
                                }
                            }

                            storage.saveTasks(updatedTasks)
                        }
                    },

                    onDelete = {

                        scope.launch {
                            val updatedTasks = tasks.filter {
                                it.id != task.id
                            }

                            storage.saveTasks(updatedTasks)

                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TaskListScreen(
    tasks: List<Task>,
    onAddClick: () -> Unit,
    onTaskClick: (Task) -> Unit
) {

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {

            Text(
                text = "Daylist",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text("Organiza tus tareas y aprovecha tu día")

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mis tareas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text("Pendientes: ${tasks.count { !it.completed }}")

            Spacer(modifier = Modifier.height(16.dp))

            if (tasks.isEmpty()) {

                Text(
                    text = "Todavía no tienes tareas. Presiona + para agregar una.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center
                )

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(tasks, key = { it.id }) { task ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTaskClick(task)
                                }
                        ) {

                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        task.title,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(task.subject)

                                    Text(
                                        if (task.completed) {
                                            "Completada"
                                        } else {
                                            "Pendiente"
                                        }
                                    )
                                }

                                Text("Ver detalles")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Volver")
        }

        Text(
            text = "Agregar tarea",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título de la tarea") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Materia") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSave(
                    title.trim(),
                    subject.trim(),
                    description.trim()
                )
            },
            enabled = title.isNotBlank() && subject.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar tarea")
        }
    }
}

@Composable
fun TaskDetailScreen(
    task: Task,
    onBack: () -> Unit,
    onCompleteChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        TextButton(onClick = onBack) {
            Text("← Volver a mis tareas")
        }

        Text(
            text = "Detalles de la tarea",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = task.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text("Materia: ${task.subject}")

        Text(
            "Descripción: ${task.description.ifBlank { "Sin descripción" }}"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = task.completed,
                onCheckedChange = onCompleteChange
            )

            Text("Marcar como completada")
        }

        Text(
            text = if (task.completed) {
                "Estado: Completada"
            } else {
                "Estado: Pendiente"
            }
        )

        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Eliminar tarea")
        }
    }
}

