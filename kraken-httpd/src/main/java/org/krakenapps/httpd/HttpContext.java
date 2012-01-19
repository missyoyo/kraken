/*
 * Copyright 2012 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.httpd;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.krakenapps.httpd.impl.ServletRegistryImpl;
import org.krakenapps.servlet.api.ServletRegistry;

public class HttpContext {
	private String name;
	private ServletContext servletContext;
	private ServletRegistry servletRegistry;
	private WebSocketManager webSocketManager;
	private ConcurrentMap<String, HttpSession> httpSessions;

	public HttpContext(String name) {
		this.name = name;
		this.servletContext = new ServletContextImpl();
		this.servletRegistry = new ServletRegistryImpl();
		this.webSocketManager = new WebSocketManager();
		this.httpSessions = new ConcurrentHashMap<String, HttpSession>();
	}

	public String getName() {
		return name;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public ServletRegistry getServletRegistry() {
		return servletRegistry;
	}

	public WebSocketManager getWebSocketManager() {
		return webSocketManager;
	}

	public ConcurrentMap<String, HttpSession> getHttpSessions() {
		return httpSessions;
	}

	@Override
	public String toString() {
		return "[" + name + ", sessions=" + httpSessions.size() + "]\n" + servletRegistry + webSocketManager;
	}
}
