package com.thankcreate.care.viewmodel;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel {
	
	public List<ItemViewModel> items;
	public List<ItemViewModel> listItems;
	public List<ItemViewModel> sinaWeiboItems;
	public List<ItemViewModel> renrenItems;
	public List<ItemViewModel> doubanItems;
	public List<ItemViewModel> rssItems;
	
	public List<PictureItemViewModel> pictureItems;
	public List<PictureItemViewModel> listPictureItems;
	public List<PictureItemViewModel> sinaWeiboPictureItems;
	public List<PictureItemViewModel> renrenPictureItems;
	public List<PictureItemViewModel> doubanPictureItems;
	
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
