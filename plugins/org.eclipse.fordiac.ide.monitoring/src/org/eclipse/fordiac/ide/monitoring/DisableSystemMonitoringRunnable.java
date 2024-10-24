/*******************************************************************************
 * Copyright (c) 2016 - 2018 fortiss GmbH, Johannes Kepler University
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alois Zoitl - initial API and implementation and/or initial documentation
 *   Alois Zoitl - Harmonized deployment and monitoring
 *******************************************************************************/
package org.eclipse.fordiac.ide.monitoring;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.fordiac.ide.deployment.exceptions.DeploymentException;
import org.eclipse.fordiac.ide.deployment.monitoringbase.MonitoringBaseElement;
import org.eclipse.fordiac.ide.model.libraryElement.Device;
import org.eclipse.fordiac.ide.model.monitoring.MonitoringElement;
import org.eclipse.fordiac.ide.ui.FordiacLogHelper;
import org.eclipse.jface.operation.IRunnableWithProgress;

class DisableSystemMonitoringRunnable implements IRunnableWithProgress {

	private final SystemMonitoringData systemMonitoringData;

	public DisableSystemMonitoringRunnable(final SystemMonitoringData systemMonitoringData) {
		this.systemMonitoringData = systemMonitoringData;
	}

	@Override
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

		final List<Device> devices = this.systemMonitoringData.getSystem().getSystemConfiguration().getDevices();
		int count = devices.size() * 2; // the * 2 is for creating the polling threads
		count += this.systemMonitoringData.getMonitoredElements().size();

		monitor.beginTask("Disable monitoring for system", count);
		stopPollingThreads(monitor);
		removeWatches(monitor);
		disconnectFromDevices(monitor);
		monitor.done();
	}

	private void disconnectFromDevices(final IProgressMonitor monitor) {
		monitor.subTask("Disconnecting the devices");
		for (final Entry<Device, DeviceMonitoringHandler> runner : systemMonitoringData.getDevMonitoringHandlers()
				.entrySet()) {
			if (monitor.isCanceled()) {
				break;
			}
			try {
				if (runner.getValue().getDevMgmInteractor().isConnected()) {
					runner.getValue().getDevMgmInteractor().disconnect();
				}
			} catch (final DeploymentException e) {
				// TODO think if error should be shown to the user
				FordiacLogHelper.logError("Could not disconnect from device", e); //$NON-NLS-1$
			}
			monitor.worked(1);
		}
		systemMonitoringData.getDevMonitoringHandlers().clear();
	}

	private void removeWatches(final IProgressMonitor monitor) {
		monitor.subTask("Connecting to the devices");
		for (final MonitoringBaseElement element : systemMonitoringData.getMonitoredElements()) {
			if (monitor.isCanceled()) {
				break;
			}
			if (element instanceof MonitoringElement) {
				monitor.subTask("Remove watch for: " + element.getPortString());
				systemMonitoringData.sendRemoveWatch(element);
				// clear the value to show that there is currently no value
				((MonitoringElement) element).setCurrentValue(""); //$NON-NLS-1$
				monitor.worked(1);
			}
		}
	}

	private void stopPollingThreads(final IProgressMonitor monitor) {
		monitor.subTask("Enabling the polling threads");
		for (final Entry<Device, DeviceMonitoringHandler> runner : systemMonitoringData.getDevMonitoringHandlers()
				.entrySet()) {
			if (monitor.isCanceled()) {
				break;
			}
			runner.getValue().disable();
			monitor.worked(1);
		}
	}

}