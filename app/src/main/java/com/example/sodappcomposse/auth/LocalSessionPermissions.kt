package com.example.sodappcomposse.auth

import androidx.compose.runtime.compositionLocalOf

/**
 * Permissions of the current session for any screen that hides UI by permission.
 * Provided by the Navigator; the default (no permissions) only applies to previews.
 */
val LocalSessionPermissions = compositionLocalOf { SessionPermissions() }
