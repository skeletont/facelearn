# refs: https://circleken.net/2020/11/post36/

from facenet_pytorch import MTCNN
#from facenet_pytorch import MTCNN, InceptionResnetV1
#from facenet_pytorch import InceptionResnetV1
from PIL import Image
import numpy as np
import torch

from facenetpytorch.models.inception_resnet_v1 import InceptionResnetV1

def convert():
    print("finish import")

    mtcnn = MTCNN()

    print("finish load mtcnn")

    resnet = InceptionResnetV1(pretrained='vggface2').eval()

    print("finish load resnet")

    # image_path = "images/post36_003.jpg"
    # image_path = "/sdcard/DCIM/MyImage.jpg"
    # image_path = "/sdcard/Python/images/03.jpg"
    # image_path = "/sdcard/Android/data/com.example.facelearn/files/MyImage.jpg"
    image_path = "/sdcard/Android/data/com.example.facelearn/files/MyImage.jpg"
    img = Image.open(image_path)
    img_cropped = mtcnn(img)

    print("finish load image")

    # # image_path_ = "images/post36_003_.jpg"
    # # image_path_ = "../assets/images/03.jpg"
    # #image_path_ = "/sdcard/Python/images/03.jpg"
    # image_path_ = "/sdcard/Android/data/com.example.facelearn/files/03.jpg"
    # img_ = Image.open(image_path_)
    # img_cropped_ = mtcnn(img_)
    #
    # print("finish load image_")

    feature_vector = resnet(img_cropped.unsqueeze(0))
    feature_vector_np = feature_vector.squeeze().to('cpu').detach().numpy().copy()
    feature_vector_np = feature_vector_np / np.linalg.norm(feature_vector_np)

    print("finish convert to vector")
    return np.array2string(feature_vector_np, separator=', ')
    # print(feature_vector_np)
    #
    # feature_vector_ = resnet(img_cropped_.unsqueeze(0))
    # feature_vector_np_ = feature_vector_.squeeze().to('cpu').detach().numpy().copy()
    # feature_vector_np_ = feature_vector_np_ / np.linalg.norm(feature_vector_np_)
    #
    # print("finish convert to vector_")
    # print(feature_vector_np_)
    #
    # #### 2つのベクトル間のコサイン類似度を取得(cosine_similarity(a, b) = a・b / |a||b|)
    # def cosine_similarity(a, b):
    #     # return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))
    #     return np.dot(a, b)
    #
    # similarity = cosine_similarity(feature_vector_np, feature_vector_np_)
    #
    # print("finish calculate similarity")
    # print(similarity)
    #
    # print("finish exe")
    # return similarity

# test
convert()