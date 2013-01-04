package com.thankcreate.care.viewmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainViewModel implements Serializable{
	
	public ArrayList<ItemViewModel> items;
	public ArrayList<ItemViewModel> listItems;
	public ArrayList<ItemViewModel> sinaWeiboItems;
	public ArrayList<ItemViewModel> renrenItems;
	public ArrayList<ItemViewModel> doubanItems;
	public ArrayList<ItemViewModel> rssItems;
	
	public ArrayList<PictureItemViewModel> pictureItems;
	public ArrayList<PictureItemViewModel> listPictureItems;
	public ArrayList<PictureItemViewModel> sinaWeiboPictureItems;
	public ArrayList<PictureItemViewModel> renrenPictureItems;
	public ArrayList<PictureItemViewModel> doubanPictureItems;
	
	public Boolean isChanged = true;

	public MainViewModel() {
		items = new ArrayList<ItemViewModel>();
		listItems = new ArrayList<ItemViewModel>();
		sinaWeiboItems = new ArrayList<ItemViewModel>();
		renrenItems = new ArrayList<ItemViewModel>();
		doubanItems = new ArrayList<ItemViewModel>();
		rssItems = new ArrayList<ItemViewModel>();
		
		pictureItems = new ArrayList<PictureItemViewModel>();
		listPictureItems = new ArrayList<PictureItemViewModel>();
		sinaWeiboPictureItems = new ArrayList<PictureItemViewModel>();
		renrenPictureItems = new ArrayList<PictureItemViewModel>();
		doubanPictureItems = new ArrayList<PictureItemViewModel>();
	}
}
