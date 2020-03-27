package com.mygamecompany.kotlinchat.utilities

object CurrentRole
{
    enum class Role
    {
        NONE,
        CLIENT,
        SERVER
    }

    private var role: Role = Role.NONE
    fun getRole(): Role = role
    fun setRole(newRole: Role) { role = newRole }
}