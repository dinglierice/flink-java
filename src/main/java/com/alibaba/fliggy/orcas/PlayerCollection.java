package com.alibaba.fliggy.orcas;

import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.alibaba.fliggy.orcas.Player.*;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/9/17 12:39
 */
public class PlayerCollection {
    private static final List<Player> allPlayers;
    private static final List<List<Player>> finishedCollections = new ArrayList<>();
    private static final List<List<Player>> allPlayersCollection = new ArrayList<>();
    private static final Map<Player, Integer> learderboard = new HashMap<>();


    static {
        allPlayers = Arrays.asList(SHD, BYM , YJB, YZY, SJF, DL);
        for (Player p : allPlayers) {
            learderboard.put(p, 0);
        }

        InputStream inputStream = ClassLoader.getSystemResourceAsStream("leaderboard.properties");
        Properties perperties = new Properties();
        try {
            perperties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String finishedCollectionsString = perperties.getProperty("FINISHED_COLLECTIONS");
        for(String s:finishedCollectionsString.split("-")) {
            List<Player> players = new ArrayList<>();
            String[] collectionAndResult = s.split(",");
            int result = 0;
            for (int i = 0; i < collectionAndResult.length; i++) {
                if (i != 3) {
                    players.add(Player.getPlayerByName(collectionAndResult[i]));
                } else {
                    result = Integer.valueOf(collectionAndResult[i]);
                }
            }
            finishedCollections.add(players);
            if (result == 1) {
                for (Player p : players) {
                    Integer score = learderboard.get(p);
                    score ++ ;
                    learderboard.put(p, score);
                }
            } else {
                for (Player p : allPlayers) {
                    if (!players.contains(p)) {
                        Integer score = learderboard.get(p);
                        score ++ ;
                        learderboard.put(p, score);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        dfs(allPlayersCollection, new ArrayList<>(), 0);

        // 获取剩余组队
        List<List<Player>> unfinishedCollections = new ArrayList<>();
        for (List<Player> players : allPlayersCollection) {
            boolean exists = false;
            for (List<Player> fPlayers: finishedCollections) {
                if (comparePlayerCollection(players, fPlayers)) {
                    exists = true;
                }
            }
            if (!exists) {
                unfinishedCollections.add(players);
            }
        }
        System.out.println("已经比完的组合:");
        System.out.println(finishedCollections);

        System.out.println("目前的积分榜:");
        System.out.println(learderboard);

        // 从中选出一个组合
        if (unfinishedCollections.size() == 0) {
            System.out.println("比赛结束啦");
            return;
        }
        List<Player> playersToPlay = unfinishedCollections.get(0);
        System.out.println("本轮比赛组合:");
        System.out.println(playersToPlay);


        // 这里开启监听，输入playerToPlay的比分
        // 最后持久化更新properties配置

    }

    private static void dfs(List<List<Player>> res , List<Player> collection, Integer currentIndex) {
        if (collection.size() == 3) {
            res.add(collection);
            return;
        }
        if (currentIndex == allPlayers.size()) {
            return;
        }
        for (int i = currentIndex; i < allPlayers.size(); i++) {
            List<Player> c = new ArrayList<>(collection);
            c.add(allPlayers.get(i));
            currentIndex ++;
            dfs(res, c , currentIndex);
        }
    }

    private static boolean comparePlayerCollection (List<Player> p1, List<Player> p2) {
        for (Player p : p1) {
            if (!p2.contains(p)) {
                return false;
            }
        }
        return true;
    }

    private static boolean ifCompared(List<List<Player>> pc, List<Player> p) {
        for(List<Player> pc1: pc) {
            if (comparePlayerCollection(p, pc1)) {
                return true;
            }
        }
        return false;
    }
}


