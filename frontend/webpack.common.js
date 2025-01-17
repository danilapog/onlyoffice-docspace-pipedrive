/**
 *
 * (c) Copyright Ascensio System SIA 2025
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/* eslint-disable */
const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyPlugin = require("copy-webpack-plugin");

module.exports = {
    entry: "./src/index.tsx",
    plugins: [
        new HtmlWebpackPlugin({
            template: "src/public/index.html",
        }),
        new CopyPlugin({
            patterns: [{ from: "src/assets" }],
        }),
    ],
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: "ts-loader",
                exclude: /node_modules/,
            },
            {
                test: /\.css$/i,
                use: ["style-loader", "css-loader", "postcss-loader"],
            },
            {
                test: /\.(png|jpg|jpeg|gif|ico)$/i,
                type: "asset/resource",
            },
             {
                test: /\.svg$/,
                use: ['@svgr/webpack'],
            },
            {
                test: /\.(woff|woff2|eot|ttf|otf)$/i,
                type: "asset/resource",
            },
        ],
    },
    resolve: {
        extensions: [".tsx", ".ts", ".js"],
        alias: {
            "@assets": path.resolve(__dirname, "src/assets"),
            "@components": path.resolve(__dirname, "src/components"),
            "@context": path.resolve(__dirname, "src/context"),
            "@layouts": path.resolve(__dirname, "src/layout"),
            "@pages": path.resolve(__dirname, "src/pages"),
            "@services": path.resolve(__dirname, "src/services"),
            "@utils": path.resolve(__dirname, "src/utils"),
        },
    },
    output: {
        filename: "[name].bundle.[chunkhash].js",
        path: path.resolve(__dirname, "build"),
        clean: true,
    },
    optimization: {
        splitChunks: {
            cacheGroups: {
                vendor: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendors',
                    chunks: 'all',
                    minSize: 100000,
                    maxSize: 250000,
                }
            }
        }
    },
};
