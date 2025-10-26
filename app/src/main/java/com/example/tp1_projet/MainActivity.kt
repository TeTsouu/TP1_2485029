package com.example.diceapp

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

private const val TAG = "LanceurDeDes"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LanceurDeDesApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanceurDeDesApp() {
    var onglet by rememberSaveable { mutableStateOf(0) }
    val titres = listOf("Remise 1 (UI)", "Remise 2 (Fonctionnelle)")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🎲 Lanceur de Dés — Projet Combiné") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = onglet) {
                titres.forEachIndexed { index, titre ->
                    Tab(
                        selected = onglet == index,
                        onClick = {
                            Log.d(TAG, "Changement d’onglet : $titre")
                            onglet = index
                        },
                        text = { Text(titre) }
                    )
                }
            }

            when (onglet) {
                0 -> Remise1_UIStatique(Modifier.fillMaxSize().padding(12.dp))
                1 -> Remise2_Fonctionnelle(Modifier.fillMaxSize().padding(12.dp))
            }
        }
    }
}

/* ==========================================================
   REMISE 1 — Interface statique (sans logique)
   ========================================================== */
@Composable
fun Remise1_UIStatique(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Remise 1 — Interface statique", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // Section : paramètres
        Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(6.dp)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Paramètres", fontWeight = FontWeight.Medium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Nombre de dés :")
                    Spacer(Modifier.width(8.dp))
                    (1..6).forEach { n ->
                        OutlinedButton(onClick = { Log.d(TAG, "[UI] Nombre de dés : $n") }) {
                            Text("$n")
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Type de dé :")
                    Spacer(Modifier.width(8.dp))
                    listOf(4, 6, 8, 10, 12, 20).forEach { faces ->
                        OutlinedButton(onClick = { Log.d(TAG, "[UI] Dé sélectionné : d$faces") }) {
                            Text("d$faces")
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tri :")
                    Spacer(Modifier.width(8.dp))
                    listOf("Aucun", "Croissant", "Décroissant").forEach { tri ->
                        OutlinedButton(onClick = { Log.d(TAG, "[UI] Tri sélectionné : $tri") }) {
                            Text(tri)
                        }
                    }
                }

                Button(
                    onClick = { Log.d(TAG, "[UI] Bouton Lancer pressé") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Lancer les dés")
                }
            }
        }

        // Section : résultats fictifs
        Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(6.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Résultats (exemple statique)", fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text("Valeurs : 3, 5, 2")
                Text("Somme : 10")
            }
        }

        Spacer(Modifier.weight(1f))
        Text("Tous les boutons génèrent un Log.d() (voir Logcat)", fontSize = 12.sp, color = Color.Gray)
    }
}

/* ==========================================================
   REMISE 2 — Version fonctionnelle (sans images)
   ========================================================== */
@Composable
fun Remise2_Fonctionnelle(modifier: Modifier = Modifier) {
    var nbDesTexte by rememberSaveable { mutableStateOf("2") }
    var nbFacesTexte by rememberSaveable { mutableStateOf("6") }
    var tri by rememberSaveable { mutableStateOf("Aucun") }
    var resultats by rememberSaveable { mutableStateOf(listOf<Int>()) }

    // validations/parsings sûrs
    val nbDes = nbDesTexte.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val nbFaces = nbFacesTexte.toIntOrNull()?.coerceAtLeast(4) ?: 6

    val orientation = LocalConfiguration.current.orientation

    // fonction lancer robuste : calcule d'abord les nouvelles valeurs, met à jour resultats,
    // calcule la somme locale pour le log (évite d'utiliser "somme" calculé AVANT mise à jour)
    val lancer: () -> Unit = {
        // Génération
        val generes = List(nbDes) { Random.nextInt(1, nbFaces + 1) }
        // Application du tri demandé
        val finalList = when (tri) {
            "Croissant" -> generes.sorted()
            "Décroissant" -> generes.sortedDescending()
            else -> generes
        }
        // Mise à jour de l'état (déclenche recomposition)
        resultats = finalList

        // Calculer la somme locale pour logging (et précision immédiate)
        val sommeLocale = finalList.sum()

        // Logs clairs
        Log.d(TAG, "[Remise2] Lancer -> nbDes=$nbDes, nbFaces=$nbFaces, tri=$tri")
        Log.d(TAG, "[Remise2] Généré (avant tri) = $generes")
        Log.d(TAG, "[Remise2] Après tri = $finalList, somme = $sommeLocale")
    }

    val somme = resultats.sum()

    // Layout (identique au précédent, mais bouton désactivé si entrée invalide)
    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ParametresSection(
                nbDesTexte = nbDesTexte,
                onNbDesChange = { nbDesTexte = it },
                nbFacesTexte = nbFacesTexte,
                onNbFacesChange = { nbFacesTexte = it },
                tri = tri,
                onTriChange = { tri = it },
                onLancer = lancer,
                enabled = (nbDes >= 1 && nbFaces >= 4)
            )
            ResultatsSection(resultats = resultats, somme = somme)
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ParametresSection(
                    nbDesTexte = nbDesTexte,
                    onNbDesChange = { nbDesTexte = it },
                    nbFacesTexte = nbFacesTexte,
                    onNbFacesChange = { nbFacesTexte = it },
                    tri = tri,
                    onTriChange = { tri = it },
                    onLancer = lancer,
                    enabled = (nbDes >= 1 && nbFaces >= 4)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                ResultatsSection(resultats = resultats, somme = somme)
            }
        }
    }
}


@Composable
fun ParametresSection(
    nbDesTexte: String,
    onNbDesChange: (String) -> Unit,
    nbFacesTexte: String,
    onNbFacesChange: (String) -> Unit,
    tri: String,
    onTriChange: (String) -> Unit,
    onLancer: () -> Unit,
    enabled: Boolean
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Paramètres", fontWeight = FontWeight.Medium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nombre de dés :")
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = nbDesTexte,
                    onValueChange = { if (it.isEmpty() || it.all { ch -> ch.isDigit() }) onNbDesChange(it) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nombre de faces :")
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = nbFacesTexte,
                    onValueChange = { if (it.isEmpty() || it.all { ch -> ch.isDigit() }) onNbFacesChange(it) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(80.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tri :")
                Spacer(Modifier.width(8.dp))
                listOf("Aucun", "Croissant", "Décroissant").forEach { opt ->
                    OutlinedButton(
                        onClick = { onTriChange(opt) },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(opt)
                    }
                }
            }

            Button(
                onClick = onLancer,
                enabled = enabled,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Lancer les dés")
            }
        }
    }
}

@Composable
fun ResultatsSection(resultats: List<Int>, somme: Int) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(6.dp)) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Résultats", fontWeight = FontWeight.Medium)
            if (resultats.isEmpty()) {
                Text("Aucun lancer effectué", fontSize = 16.sp)
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(resultats) { valeur ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = valeur.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Text("Somme : $somme", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
