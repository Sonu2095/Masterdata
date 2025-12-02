package com.avaya.amsp.masterdata.dtos;

import java.util.ArrayList;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ClusterConnectionMapping {
	private ArrayList<Long> deleteList;
	private ArrayList<Long> addList;
}
