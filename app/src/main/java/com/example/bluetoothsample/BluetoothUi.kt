package com.example.bluetoothsample

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


@Composable
fun BluetoothUiConnection(bluetoothController: BluetoothController) {
    // Contexte actuel de l'application
    val context = LocalContext.current
    // État pour contrôler la visibilité du bouton d'initialisation
    var isButtonInitVisible by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isButtonInitVisible) {
            // Bouton pour initialiser le périphérique Bluetooth avec le profil HID
            Button(
                onClick = {
                    bluetoothController.init(context.applicationContext)
                    isButtonInitVisible = false // Cache le bouton après l'initialisation
                }
            ) {
                Text(text = "Initialize Bluetooth device with HID profile")
            }
        } else {
            // Vérifie si le Bluetooth est connecté
            val btOn = bluetoothController.status is BluetoothController.Status.Connected
            if (!btOn) {
                // Bouton pour découvrir et appairer de nouveaux appareils
                Button(
                    onClick = { context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)) }
                ) {
                    Text(text = "Discover and Pair new devices")
                }
            }
            // États de connexion Bluetooth
            val waiting = bluetoothController.status is BluetoothController.Status.Waiting
            val disconnected = bluetoothController.status is BluetoothController.Status.Disconnected
            if (waiting or disconnected) {
                // Bouton pour connecter à l'hôte Bluetooth
                Button(
                    onClick = { bluetoothController.connectHost() }
                ) {
                    Text(text = "Bluetooth connect to host")
                }
            }
            // Affiche l'état actuel de la connexion Bluetooth
            Text(text = bluetoothController.status.display)

            // Icône indiquant l'état de connexion Bluetooth
            Icon(
                if (btOn) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                "bluetooth",
                modifier = Modifier.size(100.dp),
                tint = if (btOn) Color.Blue else Color.Black,
            )
            if (btOn) {
                // Bouton pour se déconnecter de l'hôte Bluetooth
                Button(
                    onClick = { bluetoothController.release() }
                ) {
                    Text(text = "Bluetooth disconnect from host")
                }
            }
        }
    }
}

@Composable
fun BluetoothDesk(bluetoothController: BluetoothController) {
    // S'assure qu'un appareil est connecté avant de continuer
    val connected = bluetoothController.status as? BluetoothController.Status.Connected ?: return

    // Contexte actuel de l'application
    val context = LocalContext.current
    // Initialisation de l'envoi de commandes clavier
    val keyboardSender = KeyboardSender(connected.btHidDevice, connected.hostDevice)

    // Gestion du profil sélectionné
    var selectedProfile by remember { mutableStateOf(1) }
    val profiles = listOf("Profile 1", "Profile 2")
    var expanded by remember { mutableStateOf(false) }

    // Fonction pour envoyer des raccourcis clavier
    fun press(shortcut: Shortcut, releaseModifiers: Boolean = true) {
        val result = keyboardSender.sendKeyboard(shortcut.shortcutKey, shortcut.modifiers, releaseModifiers)
        if (!result) Toast.makeText(context, "Can't find keymap for $shortcut", Toast.LENGTH_LONG).show()
    }

    // Actions associées à chaque profil
    val profileActions = mapOf(
        1 to { press(Shortcut(KeyEvent.KEYCODE_A)) }, // Action pour le Profil 1
        2 to { press(Shortcut(KeyEvent.KEYCODE_1)) }  // Action pour le Profil 2
    )

    // UI pour sélectionner le profil
    Text("Selected Profile: ")
    Text(profiles[selectedProfile - 1])
    Button(onClick = { expanded = true }) {
        Text("Change Profile")
    }

    // Menu déroulant pour le choix du profil
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        profiles.forEachIndexed { index, profile ->
            DropdownMenuItem(
                onClick = {
                    selectedProfile = index + 1
                    expanded = false
                },
                text = { Text(profile) }
            )
        }
    }

    fun alphanum (){
        press(Shortcut(KeyEvent.KEYCODE_A))
    }
    fun numericSequence() {
        press(Shortcut(KeyEvent.KEYCODE_1))

    }

    val buttonActions = mapOf(
        1 to listOf({ alphanum() }, { numericSequence() }), // Button 1 actions for Profile 1 and 2
        2 to listOf({ press(Shortcut(KeyEvent.KEYCODE_B)) }, { press(Shortcut(KeyEvent.KEYCODE_2)) }),
        3 to listOf({ press(Shortcut(KeyEvent.KEYCODE_C)) }, { press(Shortcut(KeyEvent.KEYCODE_3)) }),
        4 to listOf({ press(Shortcut(KeyEvent.KEYCODE_D)) }, { press(Shortcut(KeyEvent.KEYCODE_4)) }),
        5 to listOf({ press(Shortcut(KeyEvent.KEYCODE_E)) }, { press(Shortcut(KeyEvent.KEYCODE_5)) }),
        6 to listOf({ press(Shortcut(KeyEvent.KEYCODE_F)) }, { press(Shortcut(KeyEvent.KEYCODE_6)) }),
        7 to listOf({ press(Shortcut(KeyEvent.KEYCODE_G)) }, { press(Shortcut(KeyEvent.KEYCODE_7)) }),
        8 to listOf({ press(Shortcut(KeyEvent.KEYCODE_H)) }, { press(Shortcut(KeyEvent.KEYCODE_8)) })
    )

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp).fillMaxSize()) {
        Spacer(modifier = Modifier.weight(1f)) // Espacement pour aligner le contenu
        Text("Stream Deck Controls")
        Spacer(modifier = Modifier.size(10.dp))

        // Définition de la grille de boutons pour les actions
        val buttonHeightWeight = 1f
        Column {
            for (row in 1..4) {
                Row(modifier = Modifier.fillMaxWidth().weight(buttonHeightWeight), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (column in 1..2) {
                        val buttonIndex = (row - 1) * 2 + column
                        Button(
                            modifier = Modifier
                                .weight(buttonHeightWeight)
                                .padding(end = 4.dp)
                                .fillMaxHeight(),
                            onClick = { buttonActions[buttonIndex]?.get(selectedProfile - 1)?.invoke() }
                        ) {
                            Text("Action $buttonIndex")
                        }
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}

