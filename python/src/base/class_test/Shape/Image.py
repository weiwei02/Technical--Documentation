#!/usr/bin/env python3

"""
    图像压缩存储类
    :author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email> 
    :sine 2017/8/18
    :version 1.0
"""
import os
import pickle


class ImageError(Exception):
    pass


class CoordinateError(ImageError):
    pass


class NoFilenameError(Exception):
    pass


class SaveError(Exception):
    pass


class LoadError(Exception):
    pass


class ExportError(Exception):
    pass


class Image:
    def __init__(self, width, height, filename="", background="#FFFFFF"):
        self.filename = filename
        self.__background = background
        self.__data = {}
        self.__width = width
        self.__height = height
        self.__colors = {self.__background}

    @property
    def background(self):
        return self.__background
    
    @property
    def width(self):
        return self.__width
    
    @property
    def height(self):
        return self.__height
    
    @property
    def colors(self):
        return self.__colors
    
    def __getitem__(self, coordinate):
        """获取某个坐标下的颜色，可以使用项存取操作符 []"""
        self.__judge_coordinate(coordinate)
        return self.__data.get(tuple(coordinate), self.__background)
    
    def __setitem__(self, key, coordinate, color):
        self.__judge_coordinate(coordinate)
        if color == self.__background:
            self.__data.pop(tuple(coordinate), None)
        else:
            self.__data[tuple(coordinate)] = color
            self.__colors.add(color)
            
    def __judge_coordinate(self, coordinate):
        assert len(coordinate) == 2, "coordinate should be a 2-tuple"
        if (not (0 <= coordinate[0] < self.width) or
                not (0 <= coordinate[1] < self.height)):
            raise CoordinateError(str(coordinate))
        
    def __delitem__(self, coordinate):
        self.__judge_coordinate(coordinate)
        self.__data.pop(tuple(coordinate))
        
    def save(self, filename=None):
        """将图片对象保存到文件中"""
        if filename is not None:
            self.filename = filename
        if not self.filename:
            raise NoFilenameError()
        
        fh = None
        try:
            data = [self.width, self.height, self.__background, self.__data]
            fh = open(self.filename, "wb")
            pickle.dump(data, fh, pickle.HIGHEST_PROTOCOL)
        except(EnvironmentError, pickle.PicklingError) as err:
            raise SaveError(str(err))
        finally:
            if fh is not None:
                fh.close()
                
    def load(self, filename=None):
        """从文件中加载图片对象"""
        if filename is not None:
            self.filename = filename
        if not self.filename:
            raise NoFilenameError()
        
        fh = None
        try:
            fh = open(self.filename, "rb")
            data = pickle.load(fh)
            (self.__width, self.__height, self.__background, self.__data) = data
            self.__colors = (set(self.__data.values()) | {self.__background})
        except (EnvironmentError, pickle.UnpicklingError) as err:
            raise LoadError(str(err))
        finally:
            if fh is not None:
                fh.close()
                
    def export(self, filename):
        """导出图片到文件"""
        if filename.lower().endswith(".xpm"):
            self.__export_xpm(filename)
        else:
            raise ExportError("unsupported export format:" +
                              os.path.split(filename)[1])