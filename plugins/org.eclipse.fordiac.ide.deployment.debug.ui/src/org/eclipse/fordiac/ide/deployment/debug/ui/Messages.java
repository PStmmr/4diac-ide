/*******************************************************************************
 * Copyright (c) 2023 Martin Erich Jobst
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Martin Jobst - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.fordiac.ide.deployment.debug.ui;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("squid:S3008") // tell sonar the java naming convention does not make sense for this class
public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
	public static String AbstractDeploymentCommand_ExtendedDeploymentErrorMessage;
	public static String AbstractDeploymentCommand_SimpleDeploymentErrorMessage;
	public static String AbstractDeviceDeploymentCommand_DeviceName;
	public static String AbstractOnlineFBHandler_FunctionBlock;
	public static String CleanDeviceHandler_CleanDeviceError;
	public static String DeleteResourceHandler_DeleteResourceError;
	public static String DeleteResourceHandler_Resource;
	public static String KillDeviceHandler_KillDeviceError;
	public static String OnlineCreateConnectionHandler_CreateConnectionError;
	public static String OnlineCreateFBHandler_OnlineCreateFunctionBlockError;
	public static String OnlineStartFBHandler_OnlineStartFunctionBlockError;
	public static String RestartDeviceHandler_RestartError;
	public static String RuntimeLaunchConfigurationTab_DeviceTypeDialogMessage;
	public static String RuntimeLaunchConfigurationTab_DeviceTypeDialogTitle;
	public static String RuntimeLaunchConfigurationTab_DeviceTypeLabel;
	public static String RuntimeLaunchConfigurationTab_DeviceTypeMessage;
	public static String RuntimeLaunchConfigurationTab_Dots;
	public static String RuntimeLaunchConfigurationTab_LocationLabel;
	public static String RuntimeLaunchConfigurationTab_LocationMessage;
	public static String RuntimeLaunchConfigurationTab_OptionsGroup;
	public static String RuntimeLaunchConfigurationTab_ProfileLabel;
	public static String RuntimeLaunchConfigurationTab_ProfileMessage;
	public static String RuntimeLaunchConfigurationTab_ResourceTypeDialogMessage;
	public static String RuntimeLaunchConfigurationTab_ResourceTypeDialogTitle;
	public static String RuntimeLaunchConfigurationTab_ResourceTypeLabel;
	public static String RuntimeLaunchConfigurationTab_ResourceTypeMessage;
	public static String RuntimeLaunchConfigurationTab_RuntimeGroup;
	public static String RuntimeLaunchConfigurationTab_RuntimeTabName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
