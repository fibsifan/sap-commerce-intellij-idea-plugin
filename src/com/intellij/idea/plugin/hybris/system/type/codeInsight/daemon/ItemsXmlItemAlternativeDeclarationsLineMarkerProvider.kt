/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.intellij.idea.plugin.hybris.system.type.codeInsight.daemon

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils.message
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons
import com.intellij.idea.plugin.hybris.system.type.meta.TSMetaModelAccess
import com.intellij.idea.plugin.hybris.system.type.model.ItemType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.*
import com.intellij.util.xml.DomManager
import javax.swing.Icon

class ItemsXmlItemAlternativeDeclarationsLineMarkerProvider : AbstractItemsXmlLineMarkerProvider<XmlAttributeValue>() {

    override fun getName() = message("hybris.editor.gutter.ts.items.item.alternativeDeclarations.name")
    override fun getIcon(): Icon = HybrisIcons.TS_ALTERNATIVE_DECLARATION
    override fun tryCast(psi: PsiElement) = (psi as? XmlAttributeValue)
        ?.takeIf {
            val attribute = psi.parent as? XmlAttribute ?: return@takeIf false
            return@takeIf attribute.name == ItemType.CODE
                && DomManager.getDomManager(psi.project).getDomElement(attribute.parent) is ItemType
        }

    override fun collectDeclarations(psi: XmlAttributeValue): Collection<LineMarkerInfo<PsiElement>> {
        val leaf = psi.childrenOfType<XmlToken>()
            .find { it.tokenType == XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN }
            ?: return emptyList()

        val itemType = DomManager.getDomManager(psi.project).getDomElement(psi.parent.parent as XmlTag) as? ItemType
            ?: return emptyList()

        return TSMetaModelAccess.getInstance(psi.project).findMetaForDom(itemType)
            ?.retrieveAllDoms()
            ?.filter { it != itemType }
            ?.map { it.code }
            ?.mapNotNull { it.xmlAttributeValue }
            ?.takeIf { it.isNotEmpty() }
            ?.let { targets ->
                NavigationGutterIconBuilder
                    .create(icon)
                    .setTargets(targets)
                    .setPopupTitle(message("hybris.editor.gutter.ts.items.item.alternativeDeclarations.popup.title"))
                    .setTooltipText(message("hybris.editor.gutter.ts.items.item.alternativeDeclarations.tooltip.text"))
                    .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                    .createLineMarkerInfo(leaf)
            }
            ?.let { listOf(it) }
            ?: emptyList()
    }

}