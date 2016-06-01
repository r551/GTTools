/*
 * Tencent is pleased to support the open source community by making
 * Tencent GT (Version 2.4 and subsequent versions) available.
 *
 * Notwithstanding anything to the contrary herein, any previous version
 * of Tencent GT shall not be subject to the license hereunder.
 * All right, title, and interest, including all intellectual property rights,
 * in and to the previous version of Tencent GT (including any and all copies thereof)
 * shall be owned and retained by Tencent and subject to the license under the
 * Tencent GT End User License Agreement (http://gt.qq.com/wp-content/EULA_EN.html).
 * 
 * Copyright (C) 2015 THL A29 Limited, a Tencent company. All rights reserved.
 * 
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensource.org/licenses/MIT
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.tencent.wstt.gt.monitor.simple;

import com.tencent.wstt.gt.monitor.model.Group;
import com.tencent.wstt.gt.monitor.model.Key;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用Group的调用入口
 */
public class GroupManager {
	public static GroupManager INSTANCE = new GroupManager();
	
	// Group的字典容器，以group名称为key的字典
	private Map<String, Group<Key>> container = new HashMap<String, Group<Key>>();
	
	private GroupManager()
	{
		
	}
	
	public static GroupManager getInstance()
	{
		return INSTANCE;
	}
	
	public Group<Key> getGroup(String key)
	{
		return container.get(key);
	}

	synchronized public void addGroup(String key, Group<Key> group)
	{
		if (! container.containsKey(key))
		{
			container.put(key, group);
		}
	}
	
	synchronized public void removeGroup(String key)
	{
		Group<Key> group = container.remove(key);
		group.clear();
	} 
	
	synchronized public Collection<Group<Key>> getAllGroup()
	{
		return container.values();
	}
}
