package com.lq.kill.server.service;

import com.lq.kill.model.entity.ItemKill;

import java.util.List;

public interface ItemService {
    List<ItemKill> getKillItems() throws Exception;
    ItemKill getKillDetail(Integer id) throws Exception;
}
