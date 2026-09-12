package com.example.accessibility

import android.view.accessibility.AccessibilityNodeInfo

object NodeFinder {

    /**
     * Recursively search for a clickable or target node matching text/contentDescription
     */
    fun findNodeByText(root: AccessibilityNodeInfo?, targetText: String, clickOnly: Boolean = false): AccessibilityNodeInfo? {
        if (root == null || targetText.isBlank()) return null

        val cleanTarget = targetText.trim().lowercase()

        // 1. Direct search using system helper if available
        val matchedNodes = root.findAccessibilityNodeInfosByText(targetText)
        for (node in matchedNodes) {
            if (!clickOnly || node.isClickable) {
                return node
            }
            // Check clickable ancestor
            val clickableParent = findClickableParent(node)
            if (clickableParent != null) return clickableParent
        }

        // 2. Deep recursive traversal for partial / fuzzy / contentDescription matches
        return searchRecursively(root, cleanTarget, clickOnly)
    }

    private fun searchRecursively(
        node: AccessibilityNodeInfo,
        targetLower: String,
        clickOnly: Boolean
    ): AccessibilityNodeInfo? {
        val text = node.text?.toString()?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""

        val matches = text.contains(targetLower) || desc.contains(targetLower)
        if (matches) {
            if (!clickOnly || node.isClickable) {
                return node
            }
            val parent = findClickableParent(node)
            if (parent != null) return parent
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = searchRecursively(child, targetLower, clickOnly)
            if (found != null) {
                return found
            }
        }
        return null
    }

    fun findClickableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node.parent
        while (current != null) {
            if (current.isClickable) {
                return current
            }
            current = current.parent
        }
        return null
    }

    fun findFocusedEditableNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null
        if (root.isFocused && root.isEditable) {
            return root
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findFocusedEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    fun findFirstEditableNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null
        if (root.isEditable) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findFirstEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    fun collectAllVisibleTexts(root: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (root == null) return
        val text = root.text?.toString()?.trim()
        val desc = root.contentDescription?.toString()?.trim()

        if (!text.isNullOrBlank() && !list.contains(text)) {
            list.add(text)
        }
        if (!desc.isNullOrBlank() && !list.contains(desc)) {
            list.add(desc)
        }

        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            collectAllVisibleTexts(child, list)
        }
    }
}
