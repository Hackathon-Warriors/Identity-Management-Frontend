package com.thales.idverification.modules.dashboard.model

import com.thales.idverification.utils.RoleStatus

data class VillageItemModel(
    val villageName: String?,
    val status: RoleStatus?,
    val roleName: String?,
    val roleId: Int?,
    val villageId: Int?
)
