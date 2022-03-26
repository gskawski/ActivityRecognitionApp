# % zip ../CSE535Project2_GlennSkawski.zip -r * .[^.]*
# -*- coding: utf-8 -*-
"""
Created on Thu Jan 28 00:44:25 2021

@author: chakati
"""
import cv2
import numpy as np
import os
import tensorflow as tf
import pandas as pd
import sklearn
import csv

import frameextractor as frameex
import handshape_feature_extractor as hfex

## import the handfeature extractor class

# =============================================================================
# Get the penultimate layer for trainig data
# =============================================================================
# your code goes here
# Extract the middle frame of each gesture video
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
print(ROOT_DIR)


video_train_path = ROOT_DIR + '/traindata/videos/'
frame_train_path = ROOT_DIR + '/traindata/frames/'
video_test_path = ROOT_DIR + '/test/'
if not os.path.exists(video_test_path):
    os.makedirs(video_test_path)
frame_test_path = ROOT_DIR + '/test/testframes/'
if not os.path.exists(frame_test_path):
    os.makedirs(frame_test_path)

hfe = hfex.HandShapeFeatureExtractor()


def convertTrainLabels(file_name):
    str_label = file_name.split("_")[0]
    str_to_int_dict = {
        "Num0": 0,
        "Num1": 1,
        "Num2": 2,
        "Num3": 3,
        "Num4": 4,
        "Num5": 5,
        "Num6": 6,
        "Num7": 7,
        "Num8": 8,
        "Num9": 9,
        "FanDown": 10,
        "FanOn": 11,
        "FanOff": 12,
        "FanUp": 13,
        "LightOff": 14,
        "LightOn": 15,
        "SetThermo": 16}
    return str_to_int_dict.get(str_label, "Invalid")


def scanFolder(video_path, frame_path):
    train_label_arr = []
    file_name_arr = []
    extract_feature_arr = []
    count = 0
    for file_name in sorted(os.listdir(video_path)):
        if not file_name.startswith('.') and os.path.isfile(os.path.join(video_path, file_name)):
            if "traindata" in video_path:
                temp_train_label = convertTrainLabels(file_name)
                train_label_arr.append(temp_train_label)
                file_name_arr.append(file_name)
                # print(file_name)
                # print(temp_train_label)

            temp_video_path = os.path.join(video_path, file_name)
            # print(file_name)
            temp_image = cv2.imread(frameex.frameExtractor(temp_video_path, frame_path, count))
            temp_arr = hfe.extract_feature(temp_image)
            extract_feature_arr.append(temp_arr)
            count += 1
    if "traindata" in video_path:
        avg_feature_arr = []
        i = 0
        while i <= len(extract_feature_arr)-2:
            add_vectors = extract_feature_arr[i] + extract_feature_arr[i + 1] + extract_feature_arr[i + 2]
            avg_vectors = add_vectors / 3
            avg_feature_arr.append(avg_vectors)
            i += 3
    else:
        avg_feature_arr = extract_feature_arr

    if len(train_label_arr) > 0:
        avg_train_label = []
        i = 0
        while i <= len(train_label_arr)-2:
            add_train = train_label_arr[i] + train_label_arr[i + 1] + train_label_arr[i + 2]
            avg_train = add_train / 3
            avg_train_label.append(int(avg_train))
            i += 3
        avg_feature_arr.append(avg_train_label)
    return avg_feature_arr


train_pen_layer = scanFolder(video_train_path, frame_train_path)
train_labels = train_pen_layer[-1]
del train_pen_layer[-1]
# print(train_labels)
# print(train_pen_layer)
# print(len(train_pen_layer))
test_pen_layer = scanFolder(video_test_path, frame_test_path)
# print(test_pen_layer)
# print(len(test_pen_layer))

# =============================================================================
# Recognize the gesture (use cosine similarity for comparing the vectors)
# =============================================================================

cosine_similarity = []
loss = tf.keras.losses.CosineSimilarity(axis=-1)
for test_feature in test_pen_layer:
    min_sim = 1
    i = 0
    for train_feature in train_pen_layer:

        temp_cosine_sim = loss(train_feature, test_feature).numpy()
        if temp_cosine_sim < min_sim:
            min_sim = temp_cosine_sim
            label_id = train_labels[i]
        i += 1
    cosine_similarity.append([min_sim, label_id])

# print(cosine_similarity)
# print(len(cosine_similarity))

predict_arr = []
for item in cosine_similarity:
    predict_arr.append(item[1])

print(predict_arr)

predict_df = pd.DataFrame(predict_arr)
predict_df.to_csv(ROOT_DIR + '/Results.csv', index=False, header=False)
