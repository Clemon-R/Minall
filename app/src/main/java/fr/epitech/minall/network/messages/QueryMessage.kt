package fr.epitech.minall.network.messages

import fr.epitech.minall.network.IMessage

data class QueryMessage(
    val query: String
): IMessage