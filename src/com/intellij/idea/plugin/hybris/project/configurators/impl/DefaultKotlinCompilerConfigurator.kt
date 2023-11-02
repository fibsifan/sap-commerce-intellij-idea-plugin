/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.project.configurators.impl

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.yExtensionName
import com.intellij.idea.plugin.hybris.project.configurators.HybrisConfiguratorCache
import com.intellij.idea.plugin.hybris.project.configurators.KotlinCompilerConfigurator
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.properties.PropertiesService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import org.jetbrains.kotlin.idea.compiler.configuration.Kotlin2JvmCompilerArgumentsHolder
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinJpsPluginSettings
import org.jetbrains.kotlin.idea.compiler.configuration.KotlinPluginLayout
import org.jetbrains.kotlin.idea.projectConfiguration.getDefaultJvmTarget

class DefaultKotlinCompilerConfigurator : KotlinCompilerConfigurator {

    override fun configure(descriptor: HybrisProjectDescriptor, project: Project, cache: HybrisConfiguratorCache) {
        val hasKotlinnatureExtension = descriptor.modulesChosenForImport.stream()
            .anyMatch { HybrisConstants.EXTENSION_NAME_KOTLIN_NATURE == it.name }
        if (!hasKotlinnatureExtension) return

        setKotlinCompilerVersion(project, HybrisConstants.KOTLIN_COMPILER_FALLBACK_VERSION)
        setKotlinJvmTarget(project)
    }

    override fun configureAfterImport(project: Project) {
        val hasKotlinnatureExtension = ModuleManager.getInstance(project).modules
            .any { HybrisConstants.EXTENSION_NAME_KOTLIN_NATURE == it.yExtensionName() }
        if (!hasKotlinnatureExtension) return

        val compilerVersion = PropertiesService.getInstance(project)
            ?.findMacroProperty(HybrisConstants.KOTLIN_COMPILER_VERSION_PROPERTY_KEY)
            ?.value
            ?: HybrisConstants.KOTLIN_COMPILER_FALLBACK_VERSION
        setKotlinCompilerVersion(project, compilerVersion)
    }

    // Kotlin compiler version will be updated after project import / refresh in BGT
    // we have to have indexes ready to be able to get correct value of the project property responsible for custom Kotlin compiler version
    private fun setKotlinCompilerVersion(project: Project, compilerVersion: String) {
        ApplicationManager.getApplication().runReadAction {
            KotlinJpsPluginSettings.getInstance(project).update {
                version = compilerVersion
            }
        }
    }

    private fun setKotlinJvmTarget(project: Project) {
        ApplicationManager.getApplication().runReadAction {
            val projectRootManager = ProjectRootManager.getInstance(project)

            projectRootManager.projectSdk
                ?.let { sdk -> getDefaultJvmTarget(sdk, KotlinPluginLayout.ideCompilerVersion) }
                ?.let { defaultJvmTarget ->
                    Kotlin2JvmCompilerArgumentsHolder.getInstance(project).update {
                        jvmTarget = defaultJvmTarget.description
                    }
                }
        }
    }

}
