package com.example.arena.util

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Extensiones para FirebaseFirestore siguiendo el principio DRY.
 */

fun FirebaseFirestore.sedeCollection(): CollectionReference = collection("sede")
fun FirebaseFirestore.sedeDocument(id: String): DocumentReference = sedeCollection().document(id)

fun FirebaseFirestore.canchasCollection(): CollectionReference = collection("canchas")
fun FirebaseFirestore.canchaDocument(id: String): DocumentReference = canchasCollection().document(id)

fun FirebaseFirestore.reservasCollection(): CollectionReference = collection("reservas")
fun FirebaseFirestore.reservaDocument(id: String): DocumentReference = reservasCollection().document(id)

fun FirebaseFirestore.staffCollection(): CollectionReference = collection("staff")
fun FirebaseFirestore.staffDocument(id: String): DocumentReference = staffCollection().document(id)
