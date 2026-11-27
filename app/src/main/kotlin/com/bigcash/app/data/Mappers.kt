package com.bigcash.app.data

import com.bigcash.core.database.MonitoredPackageEntity
import com.bigcash.core.database.ParsingRuleEntity
import com.bigcash.core.database.RawNotificationEntity
import com.bigcash.core.database.TransactionEntity
import com.bigcash.core.model.MonitoredPackage
import com.bigcash.core.model.ParseStatus
import com.bigcash.core.model.ParsingRule
import com.bigcash.core.model.PaymentMethod
import com.bigcash.core.model.RawNotification
import com.bigcash.core.model.RuleType
import com.bigcash.core.model.Transaction
import com.bigcash.core.model.TransactionType

fun RawNotificationEntity.toDomain(): RawNotification = RawNotification(
    id = id,
    packageName = packageName,
    bankKey = bankKey,
    title = title,
    text = text,
    bigText = bigText,
    postedAt = postedAt,
    contentHash = contentHash,
    parseStatus = ParseStatus.fromKey(parseStatus),
    matchedRuleId = matchedRuleId,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    rawNotificationId = rawNotificationId,
    ruleId = ruleId,
    bankKey = bankKey,
    packageName = packageName,
    type = TransactionType.fromKey(type),
    method = PaymentMethod.fromKey(method),
    amountMinor = amountMinor,
    currency = currency,
    cardLastDigits = cardLastDigits,
    counterparty = counterparty,
    description = description,
    occurredAt = occurredAt,
    parseStatus = ParseStatus.fromKey(parseStatus),
    tags = tags,
    isManuallyEdited = isManuallyEdited,
    possibleDuplicate = possibleDuplicate,
    createdAt = createdAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    rawNotificationId = rawNotificationId,
    ruleId = ruleId,
    bankKey = bankKey,
    packageName = packageName,
    type = type.name,
    method = method.name,
    amountMinor = amountMinor,
    currency = currency,
    cardLastDigits = cardLastDigits,
    counterparty = counterparty,
    description = description,
    occurredAt = occurredAt,
    parseStatus = parseStatus.name,
    tags = tags,
    isManuallyEdited = isManuallyEdited,
    possibleDuplicate = possibleDuplicate,
    createdAt = createdAt,
)

fun ParsingRuleEntity.toDomain(): ParsingRule = ParsingRule(
    id = id,
    name = name,
    bankKey = bankKey,
    type = RuleType.fromKey(type),
    pattern = pattern,
    priority = priority,
    isActive = isActive,
    description = description,
    amountGroup = amountGroup,
    counterpartyGroup = counterpartyGroup,
    cardGroup = cardGroup,
    typeValue = typeValue,
    methodValue = methodValue,
    currencyValue = currencyValue,
)

fun ParsingRule.toEntity(): ParsingRuleEntity = ParsingRuleEntity(
    id = id,
    name = name,
    bankKey = bankKey,
    type = type.name,
    pattern = pattern,
    priority = priority,
    isActive = isActive,
    description = description,
    amountGroup = amountGroup,
    counterpartyGroup = counterpartyGroup,
    cardGroup = cardGroup,
    typeValue = typeValue,
    methodValue = methodValue,
    currencyValue = currencyValue,
)

fun MonitoredPackageEntity.toDomain(): MonitoredPackage = MonitoredPackage(
    id = id,
    packageName = packageName,
    bankKey = bankKey,
    displayName = displayName,
    isEnabled = isEnabled,
    isBuiltIn = isBuiltIn,
)

fun MonitoredPackage.toEntity(): MonitoredPackageEntity = MonitoredPackageEntity(
    id = id,
    packageName = packageName,
    bankKey = bankKey,
    displayName = displayName,
    isEnabled = isEnabled,
    isBuiltIn = isBuiltIn,
)
