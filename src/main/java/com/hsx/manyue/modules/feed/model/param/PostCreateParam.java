package com.hsx.manyue.modules.feed.model.param;

import lombok.Data;
import java.util.List;
 
@Data
public class PostCreateParam {
    private String content;
    private List<String> imageUrls;
    private String location;
}