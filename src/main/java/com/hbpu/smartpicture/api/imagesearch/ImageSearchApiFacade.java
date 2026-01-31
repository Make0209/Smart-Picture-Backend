package com.hbpu.smartpicture.api.imagesearch;

import com.hbpu.smartpicture.api.imagesearch.model.ImageSearchResult;
import com.hbpu.smartpicture.api.imagesearch.sub.GetImageFirstUrlApi;
import com.hbpu.smartpicture.api.imagesearch.sub.GetImageListApi;
import com.hbpu.smartpicture.api.imagesearch.sub.GetImagePageUrlApi;

import java.util.List;

/**
 * 以图搜图功能门面
 */
public class ImageSearchApiFacade {
    /**
     * 搜索相似图片
     *
     * @param imageUrl 目标图片Url
     * @return 返回相似图片列表
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        String url = "https://mms-graph.cdn.bcebos.com/home-pc/scan.jpeg";
        List<ImageSearchResult> imageSearchResults = searchImage(url);
        System.out.println("搜索结果：" + imageSearchResults);
    }
}
