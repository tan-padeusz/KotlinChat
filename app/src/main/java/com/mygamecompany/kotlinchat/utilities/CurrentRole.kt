package com.mygamecompany.kotlinchat.utilities

class CurrentRole
{
    enum class Role
    {
        NONE,
        CLIENT,
        SERVER
    }

    companion object
    {
        private var current: Role = Role.NONE

        fun getRole(): Role
        {
            return current
        }

        fun setRole(newRole: Role)
        {
            current = newRole
        }
    }
}