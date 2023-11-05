package com.example.nearby.main.maps;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

class CustomRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T> {
    public CustomRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        // 클러스터에 속한 아이템의 수가 2 이상일 경우에만 클러스터를 형성합니다.
        return cluster.getSize() > 1;
    }
}

