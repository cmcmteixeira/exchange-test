package lenses.exchange.model

sealed trait ManagedErrors
case object InvalidSymbols extends ManagedErrors
