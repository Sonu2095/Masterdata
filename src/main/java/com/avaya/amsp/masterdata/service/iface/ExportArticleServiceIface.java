package com.avaya.amsp.masterdata.service.iface;

public interface ExportArticleServiceIface {

	public byte[] exportArticleData();

	public byte[] exportArticleDataByCluster(String clusterId);
}
